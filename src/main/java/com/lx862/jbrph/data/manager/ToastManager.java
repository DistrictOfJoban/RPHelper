package com.lx862.jbrph.data.manager;

import com.lx862.jbrph.Util;
import com.lx862.jbrph.data.PackEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.util.HashMap;

public class ToastManager {
    private static boolean readyToSendToast = false;
    private static final HashMap<String, SystemToast> downloadToasts = new HashMap<>();

    public static void readyToSendToast() {
        readyToSendToast = true;
    }

    public static void setupNewDownloadToast(PackEntry packEntry) {
        if(!readyToSendToast) return;

        SystemToast newToast = new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("gui.jbrph.rpdownload.title"), Text.translatable("gui.jbrph.rpdownload.generic", packEntry.name));
        downloadToasts.put(packEntry.uniqueId(), newToast);
        MinecraftClient.getInstance().getToastManager().add(newToast);
    }

    public static void updateDownloadToastProgress(PackEntry packEntry, double progress) {
        SystemToast toast = downloadToasts.get(packEntry.uniqueId());
        if(toast == null) return;

        toast.setContent(
                Text.translatable("gui.jbrph.rpdownload.title"),
                Text.translatable("gui.jbrph.rpdownload.percentage", packEntry.name, Util.get1DecPlace(progress * 100))
        );
    }

    public static void upToDate(PackEntry entry) {
        if(!readyToSendToast) return;

        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(
                        SystemToast.Type.TUTORIAL_HINT,
                        Text.translatable("gui.jbrph.rpupdate.title"),
                        Text.translatable("gui.jbrph.rpupdate.uptodate", entry.name)
                )
        );
    }

    public static void cancel(String reason) {
        if(!readyToSendToast) return;

        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(
                        SystemToast.Type.PACK_LOAD_FAILURE,
                        Text.translatable("gui.jbrph.rpupdate.title"),
                        Text.translatable("gui.jbrph.rpupdate.failed", reason)
                )
        );
    }
}
