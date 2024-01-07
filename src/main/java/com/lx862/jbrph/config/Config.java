package com.lx862.jbrph.config;

import com.google.gson.*;
import com.lx862.jbrph.data.PackEntry;
import net.fabricmc.loader.api.FabricLoader;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("jbrph.json");
    private static final List<PackEntry> packEntries = new ArrayList<>();
    private static int requestTimeoutSec = 10;

    static {
        try {
            packEntries.add(new PackEntry("Joban Pack", "https://www.joban.tk/pack/Joban_Pack.zip", "https://www.joban.tk/pack/sha1.txt", "Joban_SRP.zip"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        if(!Files.exists(CONFIG_PATH)) {
            write();
        }

        try {
            final JsonObject jsonObject = JsonParser.parseString(String.join("", Files.readAllLines(CONFIG_PATH))).getAsJsonObject();
            packEntries.clear();

            jsonObject.get("packs").getAsJsonArray().forEach(e -> {
                try {
                    packEntries.add(new Gson().fromJson(e.getAsJsonObject(), PackEntry.class));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            requestTimeoutSec = jsonObject.get("requestTimeoutSec").getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void write() {
        try {
            JsonObject jsonObject = new JsonObject();
            JsonArray entryArray = new JsonArray();
            for(PackEntry packEntry : packEntries) {
                JsonObject entryObject = new Gson().toJsonTree(packEntry).getAsJsonObject();
                entryArray.add(entryObject);
            }
            jsonObject.add("packs", entryArray);
            jsonObject.addProperty("requestTimeoutSec", requestTimeoutSec);

            Files.writeString(CONFIG_PATH, new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<PackEntry> getPackEntries() {
        return packEntries;
    }

    public static int getRequestTimeoutSec() {
        return requestTimeoutSec;
    }

    public static PackEntry getPackEntry(String name) {
        String finalName = name.contains("file/") ? name.split("/")[1] : name;
        return packEntries.stream().filter(e -> e.fileName.equals(finalName)).findFirst().orElse(null);
    }

    public static boolean havePackEntryWithUrl(String url) {
        return packEntries.stream().anyMatch(e -> e.sourceUrl.toString().equals(url));
    }
}
