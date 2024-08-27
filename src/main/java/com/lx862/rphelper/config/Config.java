package com.lx862.rphelper.config;

import com.google.gson.*;
import com.lx862.rphelper.RPHelper;
import com.lx862.rphelper.data.PackEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("jbrph.json");
    private static final List<PackEntry> packEntries = new ArrayList<>();
    private static int requestTimeoutSec = 10;

    private static Identifier normalTexture = RPHelper.id("textures/normal_texture.png");
    private static Identifier errorTexture = RPHelper.id("textures/error_texture.png");
    private static Identifier iconTexture = RPHelper.id("textures/icon_texture.png");

    private static int iconSize = 25;
    private static int normalTitleColor = 0xFFFFFF;
    private static int normalDescriptionColor = 0xFFFFFF;
    private static int errorTitleColor = 0xFF0000;
    private static int errorDescriptionColor = 0xFF0000;
    private static int width = 180;
    private static int height = 32;
    private static long duration = 5000L;

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

            if (!jsonObject.has("appearance")) {
                JsonObject defaultAppearance = createDefaultAppearance();
                jsonObject.add("appearance", defaultAppearance);
                Files.writeString(CONFIG_PATH, new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject));
            }

            JsonObject appearanceObject = jsonObject.getAsJsonObject("appearance");
            if (appearanceObject != null) {
                normalTexture = new Identifier(
                        appearanceObject.getAsJsonObject("normal_texture").get("namespace").getAsString(),
                        appearanceObject.getAsJsonObject("normal_texture").get("texture_path").getAsString()
                );
                errorTexture = new Identifier(
                        appearanceObject.getAsJsonObject("error_texture").get("namespace").getAsString(),
                        appearanceObject.getAsJsonObject("error_texture").get("texture_path").getAsString()
                );
                iconTexture = new Identifier(
                    appearanceObject.getAsJsonObject("icon_texture").get("icon_namespace").getAsString(),
                    appearanceObject.getAsJsonObject("icon_texture").get("icon_path").getAsString()
                );
                iconSize = appearanceObject.get("icon_size").getAsInt();
                normalTitleColor = hexToDecimal(appearanceObject.get("normal_title_color").getAsString());
                normalDescriptionColor = hexToDecimal(appearanceObject.get("normal_description_color").getAsString());
                errorTitleColor = hexToDecimal(appearanceObject.get("error_title_color").getAsString());
                errorDescriptionColor = hexToDecimal(appearanceObject.get("error_description_color").getAsString());
                width = appearanceObject.get("width").getAsInt();
                height = appearanceObject.get("height").getAsInt();
                duration = appearanceObject.get("duration").getAsLong();
            }
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

            JsonObject defaultAppearance = createDefaultAppearance();
            jsonObject.add("appearance", defaultAppearance);

            Files.writeString(CONFIG_PATH, new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Identifier getNormalTexture() {
        return normalTexture;
    }

    public static Identifier getErrorTexture() {
        return errorTexture;
    }

    public static Identifier getIconTexture() {
        return iconTexture;
    }

    public static int getIconSize() {
        return iconSize;
    }

    public static int getNormalTitleColor() {
        return normalTitleColor;
    }

    public static int getNormalDescriptionColor() {
        return normalDescriptionColor;
    }

    public static int getErrorTitleColor() {
        return errorTitleColor;
    }

    public static int getErrorDescriptionColor() {
        return errorDescriptionColor;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static long getDuration() {
        return duration;
    }

    public static List<PackEntry> getPackEntries() {
        return packEntries;
    }

    public static int getRequestTimeoutSec() {
        return requestTimeoutSec;
    }

    public static PackEntry getPackEntry(String name) {
        String finalName = name.contains("file/") ? name.split("/")[1] : name;
        return packEntries.stream().filter(e -> e.getFileName().equals(finalName)).findFirst().orElse(null);
    }

    public static boolean havePackEntryWithUrl(String url) {
        return packEntries.stream().anyMatch(e -> e.sourceUrl.toString().equals(url));
    }

    private static String decimalToHex(int decimal) {
        return "0x" + Integer.toHexString(decimal).toUpperCase();
    }

    private static int hexToDecimal(String hex) {
        return Integer.decode(hex);
    }

    private static JsonObject createDefaultAppearance() {
        JsonObject appearanceObject = new JsonObject();
    
        JsonObject normalTextureObject = new JsonObject();
        normalTextureObject.addProperty("namespace", normalTexture.getNamespace());
        normalTextureObject.addProperty("texture_path", normalTexture.getPath());
        appearanceObject.add("normal_texture", normalTextureObject);
    
        JsonObject errorTextureObject = new JsonObject();
        errorTextureObject.addProperty("namespace", errorTexture.getNamespace());
        errorTextureObject.addProperty("texture_path", errorTexture.getPath());
        appearanceObject.add("error_texture", errorTextureObject);

        JsonObject iconTextureObject = new JsonObject();
        iconTextureObject.addProperty("icon_namespace", iconTexture.getNamespace());
        iconTextureObject.addProperty("icon_path", iconTexture.getPath());
        appearanceObject.add("icon_texture", iconTextureObject);

        appearanceObject.addProperty("icon_size", iconSize);
        appearanceObject.addProperty("normal_title_color", decimalToHex(normalTitleColor));
        appearanceObject.addProperty("normal_description_color", decimalToHex(normalDescriptionColor));
        appearanceObject.addProperty("error_title_color", decimalToHex(errorTitleColor));
        appearanceObject.addProperty("error_description_color", decimalToHex(errorDescriptionColor));
        appearanceObject.addProperty("width", width);
        appearanceObject.addProperty("height", height);
        appearanceObject.addProperty("duration", duration);
    
        return appearanceObject;
    }
}