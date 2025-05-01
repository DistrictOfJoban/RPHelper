package com.lx862.rphelper.packet;

import com.lx862.rphelper.RPHelper;
import com.lx862.rphelper.data.Log;
import com.lx862.rphelper.data.manager.PackManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
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

        ClientPlayNetworking.registerGlobalReceiver(ResourcePackSyncPacket.ID, (payload, context) -> {
            MinecraftClient.getInstance().execute(() -> {
                Log.info("Received pack update request from server.");

                CompletableFuture.runAsync(() -> {
                    PackManager.downloadOrUpdate(false);
                });
            });
        });
    }
}
