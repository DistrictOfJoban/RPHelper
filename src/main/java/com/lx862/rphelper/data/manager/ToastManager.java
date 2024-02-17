package com.lx862.rphelper.data.manager;

import com.lx862.rphelper.Util;
import com.lx862.rphelper.data.EnqueuedToast;
import com.lx862.rphelper.data.PackEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToastManager {
    private static boolean readyToSendToast = false;
    private static final HashMap<String, SystemToast> downloadToasts = new HashMap<>();
    private static final List<EnqueuedToast> queuedToasts = new ArrayList<>();

    public static void readyToSendToast() {
        readyToSendToast = true;
        flushToasts();
    }

    public static void setupNewDownloadToast(PackEntry packEntry) {
        if(!readyToSendToast) return;

        SystemToast newToast = new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("gui.jbrph.rpdownload.title"), Text.translatable("gui.jbrph.rpdownload.description1"));
        downloadToasts.put(packEntry.uniqueId(), newToast);
        MinecraftClient.getInstance().getToastManager().add(newToast);
    }

    public static void updateDownloadToastProgress(PackEntry packEntry, double progress) {
        SystemToast toast = downloadToasts.get(packEntry.uniqueId());
        if(toast == null) return;

        toast.setContent(
                Text.translatable("gui.jbrph.rpdownload.title"),
                Text.translatable("gui.jbrph.rpdownload.description2", packEntry.name, Util.get1DecPlace(progress * 100))
        );
    }

    public static void upToDate(PackEntry entry) {
        if(!readyToSendToast) return;

        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(
                        SystemToast.Type.TUTORIAL_HINT,
                        Text.translatable("gui.jbrph.rpupdate.title", entry.name),
                        Text.translatable("gui.jbrph.rpupdate.uptodate")
                )
        );
    }

    public static void fail(String packName, String reason) {
        queueToast(
                new EnqueuedToast(
                        SystemToast.Type.PACK_LOAD_FAILURE,
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
