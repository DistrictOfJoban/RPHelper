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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PackManager {
    public static final Path RESOURCE_PACK_LOCATION = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    private static final List<Runnable> unloadCallback = new ArrayList<>();

    public static void downloadOrUpdate(boolean init) {
        List<PackEntry> packsToBeDownloaded = new ArrayList<>();

        for(PackEntry packEntry : Config.getPackEntries()) {
            if(equivPackExists(packEntry)) {
                logPackInfo(packEntry, "Equivalent pack loaded, not checking.");
                packEntry.markReady(true);
                continue;
            }

            File packFile = RESOURCE_PACK_LOCATION.resolve(packEntry.getFileName()).toFile();
            HashComparisonResult hashResult = HashManager.compareHash(packEntry, packFile, true);

            if (hashResult == HashComparisonResult.MATCH || hashResult == HashComparisonResult.NOT_AVAIL) {
                // Up to date
                packEntry.markReady(true);
                if(!init) ToastManager.upToDate(packEntry);
            } else {
                packsToBeDownloaded.add(packEntry);
            }
        }

        for(PackEntry packEntry : packsToBeDownloaded) {
            File packFile = RESOURCE_PACK_LOCATION.resolve(packEntry.getFileName()).toFile();

            // Download
            CompletableFuture.runAsync(() -> {
                logPackInfo(packEntry, "Will be downloaded.");

                long curTime = System.currentTimeMillis();
                File downloadedLocation = downloadPack(packEntry, packFile);
                long timeDiff = System.currentTimeMillis() - curTime;
                logPackInfo(packEntry, "Took " + (timeDiff / 1000.0) + "s");

                if(packFile.exists()) { // Files on Windows are locked, we have to unload the rp first before replacing
                    unloadCallback.add(() -> {
                        try {
                            Files.deleteIfExists(packFile.toPath());
                            Files.move(downloadedLocation.toPath(), packFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    try {
                        Files.move(downloadedLocation.toPath(), packFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(Config.getPackEntries().stream().allMatch(PackEntry::isReady)) {
                    packDownloaded();
                }
            });
        }
    }

    private static void packDownloaded() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if(!unloadCallback.isEmpty()) {
            MinecraftClient.getInstance().options.resourcePacks.clear();
            Log.info("Clearing all resource pack");

            for(Runnable callback : unloadCallback) {
                callback.run();
            }
            unloadCallback.clear();

            mc.reloadResources().whenComplete((r, v) -> {
                Log.info("Reapplying resource pack");
                PackApplicationManager.reloadPackDueToUpdate();
            });
        } else {
            Log.info("Reloading resource pack");
            PackApplicationManager.reloadPackDueToUpdate();
        }
    }

    public static File downloadPack(PackEntry packEntry, File outputLocation) {
        ToastManager.setupNewDownloadToast(packEntry);
        File tmpOutputLocation = outputLocation.getParentFile().toPath().resolve(UUID.randomUUID().toString()).toFile();

        try {
            final long[] lastMs = {System.currentTimeMillis()};

            DownloadManager.download(packEntry.sourceUrl, tmpOutputLocation, (prg) -> {
                // Print every 500ms
                if (System.currentTimeMillis() - lastMs[0] > 500) {
                    lastMs[0] = System.currentTimeMillis();
                    logPackInfo(packEntry, "Download Progress: " + Util.get1DecPlace(prg * 100) + "%");
                }

                ToastManager.updateDownloadToastProgress(packEntry, prg);
            }, (errorMsg) -> {
                if(errorMsg != null) {
                    logPackWarn(packEntry, "Failed to download resource pack!");
                    packEntry.markReady(false);
                    ToastManager.fail(packEntry.name, errorMsg);
                    return;
                }

                ToastManager.updateDownloadToastProgress(packEntry, 1);

                if(HashManager.compareHash(packEntry, tmpOutputLocation, false) == HashComparisonResult.MISMATCH) {
                    logPackWarn(packEntry, "Resource pack downloaded but the file hash does not match, not applying!");
                    packEntry.markReady(false);
                    ToastManager.fail(packEntry.name, "Hash Mismatch, file might be corrupted.");

                    try {
                        Files.delete(tmpOutputLocation.toPath());
                    } catch (IOException ignored) {

                    }
                    return;
                }

                logPackInfo(packEntry, "Download successful!");
                packEntry.markReady(true);
            });
        } catch (Exception e) {
            Log.LOGGER.error(e);
            ToastManager.fail(packEntry.name, "Please check console for error.");
        }
        return tmpOutputLocation;
    }

    public static boolean equivPackExists(PackEntry entry) {
        if(entry.equivPacks != null) {
            for(File file : RESOURCE_PACK_LOCATION.toFile().listFiles()) {
                if (file.isDirectory() && Arrays.asList(entry.equivPacks).contains("file/" + file.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void logPackInfo(PackEntry entry, String content) {
        Log.info("[" + entry.name + "] " + content);
    }

    public static void logPackWarn(PackEntry entry, String content) {
        Log.warn("[" + entry.name + "] " + content);
    }
}
