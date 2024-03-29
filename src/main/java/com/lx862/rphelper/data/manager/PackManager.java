package com.lx862.rphelper.data.manager;

import com.lx862.rphelper.Util;
import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.data.HashComparisonResult;
import com.lx862.rphelper.data.Log;
import com.lx862.rphelper.data.PackEntry;
import com.lx862.rphelper.network.DownloadManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PackManager {
    public static final Path RESOURCE_PACK_LOCATION = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    public static boolean stillDownloading = false;
    private static final Set<String> readyToBeUsedPacks = new HashSet<>();

    public static void downloadOrUpdate() {
        for(PackEntry packEntry : Config.getPackEntries()) {
            File packFile = RESOURCE_PACK_LOCATION.resolve(packEntry.getFileName()).toFile();

            HashComparisonResult hashResult = HashManager.compareRemoteHash(packEntry, packFile, true);
            if (hashResult == HashComparisonResult.MATCH) {
                // Up to date
                markPackAsReady(packEntry);

                MinecraftClient.getInstance().execute(() -> {
                    ToastManager.upToDate(packEntry);
                });
            } else {
                CompletableFuture.runAsync(() -> {
                    // Download
                    logPackInfo(packEntry, "The resource pack will be downloaded.");

                    long curTime = System.currentTimeMillis();
                    downloadPack(packEntry, packFile);
                    long timeDiff = System.currentTimeMillis() - curTime;
                    logPackInfo(packEntry, "Took " + (timeDiff / 1000.0) + "s");
                });
            }
        }
    }
    public static void downloadPack(PackEntry packEntry, File outputLocation) {
        ToastManager.setupNewDownloadToast(packEntry);

        try {
            final long[] lastMs = {System.currentTimeMillis()};

            DownloadManager.download(packEntry.sourceUrl, outputLocation, (prg) -> {
                // Print every 500ms
                if (System.currentTimeMillis() - lastMs[0] > 500) {
                    lastMs[0] = System.currentTimeMillis();
                    logPackInfo(packEntry, "Download Progress: " + Util.get1DecPlace(prg * 100) + "%");
                }

                ToastManager.updateDownloadToastProgress(packEntry, prg);
                PackManager.stillDownloading = true;
            }, (errorMsg) -> {
                PackManager.stillDownloading = false;

                if(errorMsg == null) {
                    ToastManager.updateDownloadToastProgress(packEntry, 1);
                    logPackInfo(packEntry, "The resource pack has been downloaded successfully!");

                    if(HashManager.compareRemoteHash(packEntry, outputLocation) == HashComparisonResult.MISMATCH) {
                        logPackWarn(packEntry, "The resource pack has been downloaded successfully but the hash does not match, it will not be applied!");
                        packNotReady(packEntry);
                        ToastManager.fail(packEntry.name, "The resource pack is corrupted!");
                    } else {
                        markPackAsReady(packEntry);
                        ServerLockManager.reloadPackDueToUpdate();
                        
                    }
                } else {
                    logPackWarn(packEntry, "Failed to download resource pack!");
                    packNotReady(packEntry);
                    ToastManager.fail(packEntry.name, errorMsg);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void markPackAsReady(PackEntry packEntry) {
        readyToBeUsedPacks.add(packEntry.uniqueId());
    }

    private static void packNotReady(PackEntry packEntry) {
        readyToBeUsedPacks.remove(packEntry.uniqueId());
    }

    public static boolean isPackReady(PackEntry entry) {
        return readyToBeUsedPacks.contains(entry.uniqueId());
    }

    public static void logPackInfo(PackEntry entry, String content) {
        Log.info("[" + entry.name + "] " + content);
    }

    public static void logPackWarn(PackEntry entry, String content) {
        Log.warn("[" + entry.name + "] " + content);
    }

    public static void logPackError(PackEntry entry, String content) {
        Log.error("[" + entry.name + "] " + content);
    }
}
