package com.lx862.rphelper.data.manager;

import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.data.PackEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackApplicationManager {
    public static void reloadPackDueToUpdate() {
        updatePackState(ReloadType.ALWAYS);
    }

    public static void refreshPackState() {
        updatePackState(ReloadType.NEVER);
    }

    private static void updatePackState(ReloadType reloadType) {
        MinecraftClient mc = MinecraftClient.getInstance();

        List<String> oldPacks = new ArrayList<>(mc.options.resourcePacks);

        for(PackEntry packEntry : Config.getPackEntries()) {
            String packName = "file/" + packEntry.getFileName();
            mc.options.resourcePacks.remove(packName);
            mc.options.incompatibleResourcePacks.remove(packName);
        }

        List<PackEntry> sortedEntries = new ArrayList<>(Config.getPackEntries());
        Collections.reverse(sortedEntries);

        for(PackEntry packEntry : sortedEntries) {
            if(!packEntry.isReady()) continue;

            String packName = "file/" + packEntry.getFileName();
            boolean packApplied = mc.options.resourcePacks.contains(packName);

            if(!packApplied && !PackManager.equivPackExists(packEntry)) {
                mc.options.resourcePacks.add(packName);
                mc.options.incompatibleResourcePacks.add(packName);
            }

            ResourcePackManager resourcePackManager = mc.getResourcePackManager();
            resourcePackManager.scanPacks();
            mc.options.addResourcePackProfilesToManager(resourcePackManager);
        }

        boolean packChanged = oldPacks.containsAll(mc.options.resourcePacks) && mc.options.resourcePacks.containsAll(oldPacks);

        if((packChanged && reloadType == ReloadType.ON_CHANGED) || reloadType == ReloadType.ALWAYS) {
            MinecraftClient.getInstance().reloadResources();
        }
    }

    public enum ReloadType {
        NEVER,
        ON_CHANGED,
        ALWAYS
    }
}
