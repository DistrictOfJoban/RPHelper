package com.lx862.rphelper.data.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.data.Log;
import com.lx862.rphelper.data.PackEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePackManager;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class ServerLockManager {
    private static final HashMap<String, String[]> serverLocks = new HashMap<>();

    public static void reloadPackDueToUpdate() {
        updatePackState(false, true);
    }

    public static void updatePackState(boolean dontReload) {
        updatePackState(dontReload, false);
    }

    private static void updatePackState(boolean dontReload, boolean forceReload) {
        refreshServerLockList();
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean packChanged = false;

        for(PackEntry packEntry : Config.getPackEntries()) {
            if(!packEntry.ready) continue;

            String packName = "file/" + packEntry.getFileName();
            ServerInfo currentServerEntry = mc.getCurrentServerEntry();
            String[] serverLock = serverLocks.get(packEntry.uniqueId());

            boolean serverMatched = false;
            if(serverLock == null) {
                serverMatched = true;
            } else if(currentServerEntry != null) {
                serverMatched = Arrays.asList(serverLock).contains(currentServerEntry.address);
            }

            boolean packShouldBeActive = mc.world == null || serverMatched;
            boolean packApplied = mc.getResourcePackManager().getEnabledNames().contains(packName);

            if(packApplied && !packShouldBeActive) {
                mc.options.resourcePacks.remove(packName);
            }

            if(!packApplied && packShouldBeActive && !PackManager.equivPackLoaded(packEntry)) {
                mc.options.resourcePacks.add(packName);
            }

            ResourcePackManager resourcePackManager = mc.getResourcePackManager();
            resourcePackManager.scanPacks();
            mc.options.addResourcePackProfilesToManager(resourcePackManager);

            if(packApplied != packShouldBeActive || forceReload) {
                packChanged = true;
            }
        }

        if(packChanged && !dontReload) {
            MinecraftClient.getInstance().reloadResources();
        }
    }

    public static void refreshServerLockList() {
        serverLocks.clear();
        PackManager.loopPack((entry, rp) -> {
            if(!(rp instanceof AbstractFileResourcePack frp)) return;
            InputSupplier<InputStream> ip = frp.openRoot("pack.mcmeta");
            if(ip == null) return;

            try (InputStream is = ip.get()) {
                String str = IOUtils.toString(is, StandardCharsets.UTF_8);
                JsonObject jsonObject = JsonParser.parseString(str).getAsJsonObject().get("pack").getAsJsonObject();
                if(jsonObject.has("serverWhitelist")) {
                    JsonArray array = jsonObject.getAsJsonArray("serverWhitelist");
                    String[] newIpArray = new String[array.size()];
                    for(int i = 0; i < array.size(); i++) {
                        newIpArray[i] = array.get(i).getAsString();
                    }
                    serverLocks.put(entry.uniqueId(), newIpArray);
                }
            } catch (Exception e) {
                Log.LOGGER.error(e);
            }
        });
    }
}
