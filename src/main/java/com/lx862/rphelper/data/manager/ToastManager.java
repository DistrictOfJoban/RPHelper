package com.lx862.rphelper.data.manager;

import com.lx862.rphelper.Util;
import com.lx862.rphelper.data.EnqueuedToast;
import com.lx862.rphelper.data.PackEntry;
import com.lx862.rphelper.custom.CustomToast;
import com.lx862.rphelper.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToastManager {
    private static boolean readyToSendToast = false;
    private static final HashMap<String, CustomToast> downloadToasts = new HashMap<>();
    private static final List<EnqueuedToast> queuedToasts = new ArrayList<>();

    public static void readyToSendToast() {
        readyToSendToast = true;
        flushToasts();
    }

    public static void setupNewDownloadToast(PackEntry packEntry) {
        if(!readyToSendToast) return;

        CustomToast newToast = new CustomToast(
                Text.translatable("gui.jbrph.rpdownload.title"),
                Text.translatable("gui.jbrph.rpdownload.description1"),
                Config.getDuration(), Config.getNormalTitleColor(), Config.getNormalDescriptionColor(), Config.getNormalTexture(), Config.getIconTexture(), Config.getIconSize(), Config.getWidth(), Config.getHeight());
        downloadToasts.put(packEntry.uniqueId(), newToast);
        MinecraftClient.getInstance().getToastManager().add(newToast);
    }

    public static void updateDownloadToastProgress(PackEntry packEntry, double progress) {
        CustomToast toast = downloadToasts.get(packEntry.uniqueId());
        if(toast == null) return;

        toast.setContent(
                Text.translatable("gui.jbrph.rpdownload.title"),
                Text.translatable("gui.jbrph.rpdownload.description2", packEntry.name, Util.get1DecPlace(progress * 100))
        );

        if (progress >= 1.0) {
            toast.time = 1;
            toast.duration = 0;
        }
    }

    public static void upToDate(PackEntry entry) {
        if(!readyToSendToast) return;

        MinecraftClient.getInstance().getToastManager().add(
                new CustomToast(
                        Text.translatable("gui.jbrph.rpupdate.title", entry.name),
                        Text.translatable("gui.jbrph.rpupdate.uptodate"),
                        Config.getDuration(), Config.getNormalTitleColor(), Config.getNormalDescriptionColor(), Config.getNormalTexture(), Config.getIconTexture(), Config.getIconSize(), Config.getWidth(), Config.getHeight())
        );
    }

    public static void fail(String packName, String reason) {
        queueToast(
                new EnqueuedToast(
                        Text.translatable("gui.jbrph.rpfail.title"),
                        Text.translatable("gui.jbrph.rpfail.description", packName, reason)
                )
        );
    }

    public static void queueToast(EnqueuedToast toast) {
        if(readyToSendToast) {
            MinecraftClient.getInstance().getToastManager().add(toast.construct());
        } else {
            queuedToasts.add(toast);
        }
    }

    private static void flushToasts() {
        for(EnqueuedToast toast : queuedToasts) {
            MinecraftClient.getInstance().getToastManager().add(toast.construct());
        }
        queuedToasts.clear();
    }
}