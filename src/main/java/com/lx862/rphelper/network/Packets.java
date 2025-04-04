package com.lx862.rphelper.network;

import com.lx862.rphelper.RPHelper;
import com.lx862.rphelper.data.Log;
import com.lx862.rphelper.data.manager.PackManager;
import com.lx862.rphelper.data.manager.PackApplicationManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class Packets {
    public static final Identifier SEND_UPDATE_RP = RPHelper.id("rp_updated");

    public static void registerClient() {
//        ClientPlayConnectionEvents.JOIN.register((networkHandler, packetSender, client) -> {
//            Log.info("Validating RP State");
//            client.execute(() -> {
//                PackApplicationManager.updatePackState(false);
//            });
//        });

        ClientPlayNetworking.registerGlobalReceiver(SEND_UPDATE_RP, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                if(client.player == null) return;
                Log.info("Received pack update request from server.");

                CompletableFuture.runAsync(() -> {
                    PackManager.downloadOrUpdate(false);
                });
            });
        });
    }
}
