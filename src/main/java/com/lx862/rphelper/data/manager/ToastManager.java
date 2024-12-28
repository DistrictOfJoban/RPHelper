package com.lx862.rphelper.data.manager;

import com.lx862.rphelper.Util;
import com.lx862.rphelper.data.ToastWrapper;
import com.lx862.rphelper.data.PackEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToastManager {
    private static boolean readyToSendToast = false;
    private static final HashMap<String, ToastWrapper> downloadToasts = new HashMap<>();
    private static final List<ToastWrapper> queuedToasts = new ArrayList<>();

    public static void readyToSendToast() {
        readyToSendToast = true;
        flushToasts();
    }

    public static void setupNewDownloadToast(PackEntry packEntry) {
        ToastWrapper toast = addToast(new ToastWrapper(
                Text.translatable("gui.rphelper.rpdownload.title"),
                Text.translatable("gui.rphelper.rpdownload.description1"),
                false
        ));
        downloadToasts.put(packEntry.uniqueId(), toast);
    }

    public static void updateDownloadToastProgress(PackEntry packEntry, double progress) {
        ToastWrapper toast = downloadToasts.get(packEntry.uniqueId());
        if(toast != null) {
            if(progress >= 1) {
                downloadToasts.remove(packEntry.uniqueId());
                queuedToasts.remove(toast);
            }

            if(toast.build != null) {
                toast.build.setContent(
                        Text.translatable("gui.rphelper.rpdownload.title"),
                        Text.translatable("gui.rphelper.rpdownload.description2", packEntry.name, Util.get1DecPlace(progress * 100))
                );
            }
        }
    }

    public static void upToDate(PackEntry entry) {
        addToast(new ToastWrapper(
            Text.translatable("gui.rphelper.rpupdate.title", entry.name),
            Text.translatable("gui.rphelper.rpupdate.uptodate"),
            false
        ));
    }

    public static void fail(String packName, String reason) {
        addToast(new ToastWrapper(
            Text.translatable("gui.rphelper.rpfail.title"),
            Text.translatable("gui.rphelper.rpfail.description", packName, reason),
                true
        ));
    }

    public static ToastWrapper addToast(ToastWrapper toast) {
        if(readyToSendToast) {
            MinecraftClient.getInstance().getToastManager().add(toast.build());
        } else {
            queuedToasts.add(toast);
        }
        return toast;
    }

    private static void flushToasts() {
        for(ToastWrapper toast : queuedToasts) {
            MinecraftClient.getInstance().getToastManager().add(toast.build());
        }
        queuedToasts.clear();
    }
}