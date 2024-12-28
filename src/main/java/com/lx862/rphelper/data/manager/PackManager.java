package com.lx862.rphelper.data.manager;

import com.lx862.rphelper.Util;
import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.data.HashComparisonResult;
import com.lx862.rphelper.data.Log;
import com.lx862.rphelper.data.PackEntry;
import com.lx862.rphelper.network.DownloadManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class PackManager {
    public static final Path RESOURCE_PACK_LOCATION = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

    public static void downloadOrUpdate(boolean init) {
        for(PackEntry packEntry : Config.getPackEntries()) {
            if(equivPackLoaded(packEntry)) {
                logPackInfo(packEntry, "Equivalent pack loaded, not checking.");
                continue;
            }

            File packFile = RESOURCE_PACK_LOCATION.resolve(packEntry.getFileName()).toFile();
            HashComparisonResult hashResult;
            if(packEntry.sha1Url != null) {
                hashResult = HashManager.compareRemoteHash(packEntry, packFile, true);
            } else {
                if(packEntry.sha1 == null) {
                    hashResult = HashComparisonResult.NOT_AVAIL;
                } else {
                    hashResult = HashManager.compareLocalHash(packEntry, packFile);
                }
            }

            if (hashResult == HashComparisonResult.MATCH || hashResult == HashComparisonResult.NOT_AVAIL) {
                // Up to date
                packEntry.ready = true;
                if(!init) ToastManager.upToDate(packEntry);
            } else {
                // Download
                CompletableFuture.runAsync(() -> {
                    logPackInfo(packEntry, "Will be downloaded.");

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
                    packEntry.ready = false;
                    ToastManager.fail(packEntry.name, errorMsg);
                    return;
                }

                MinecraftClient mc = MinecraftClient.getInstance();
                ToastManager.updateDownloadToastProgress(packEntry, 1);

                if(HashManager.compareRemoteHash(packEntry, tmpOutputLocation) == HashComparisonResult.MISMATCH) {
                    logPackWarn(packEntry, "Resource pack downloaded but the file hash does not match, not applying!");
                    packEntry.ready = false;
                    ToastManager.fail(packEntry.name, "Hash Mismatch, file might be corrupted.");
                    return;
                }

                logPackInfo(packEntry, "Download successful!");
                packEntry.ready = true;

                if(outputLocation.exists()) { // Files on Windows are locked, we have to unload the rp first before replacing
                    List<String> oldPackList = mc.options.resourcePacks;
                    mc.options.resourcePacks.clear();
                    Log.info("Clearing all resource pack");

                    mc.reloadResources().whenComplete((r, v) -> {
                        try {
                            Files.deleteIfExists(outputLocation.toPath());
                            Files.move(tmpOutputLocation.toPath(), outputLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Log.info("Reapplying resource pack");
                        mc.options.resourcePacks.addAll(oldPackList);
                        ServerLockManager.reloadPackDueToUpdate();
                    });
                } else {
                    try {
                        Files.move(tmpOutputLocation.toPath(), outputLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    ServerLockManager.reloadPackDueToUpdate();
                }
            });
        } catch (Exception e) {
            Log.LOGGER.error(e);
            ToastManager.fail(packEntry.name, "Please check console for error.");
        }
    }

    public static boolean equivPackLoaded(PackEntry entry) {
        if(entry.equivPacks != null) {
            for(String pack : MinecraftClient.getInstance().options.resourcePacks) {
                if(Arrays.asList(entry.equivPacks).contains(pack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void loopPack(BiConsumer<PackEntry, ResourcePack> callback) {
        Collection<ResourcePackProfile> profiles = MinecraftClient.getInstance().getResourcePackManager().getProfiles();
        for(ResourcePackProfile resourcePackProfile : profiles) {
            PackEntry entry = Config.getPackEntry(resourcePackProfile.getName());
            if(entry == null) {
                continue;
            }

            try (ResourcePack rp = resourcePackProfile.createResourcePack()) {
                callback.accept(entry, rp);
            }
        }
    }

    public static void logPackInfo(PackEntry entry, String content) {
        Log.info("[" + entry.name + "] " + content);
    }

    public static void logPackWarn(PackEntry entry, String content) {
        Log.warn("[" + entry.name + "] " + content);
    }
}
