package com.lx862.jbrph.network;

import com.lx862.jbrph.data.Log;
import com.lx862.jbrph.data.manager.PackManager;
import com.lx862.jbrph.data.manager.ServerLockManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class Packets {
    public static final Identifier SEND_UPDATE_RP = new Identifier("jbrph", "rp_updated");

    public static void registerClient() {
        ClientPlayConnectionEvents.JOIN.register((networkHandler, packetSender, client) -> {
            Log.info("Validating RP State");
            client.execute(() -> {
                ServerLockManager.updatePackState(false);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SEND_UPDATE_RP, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                if(client.player == null) return;

                CompletableFuture.runAsync(() -> {
                    PackManager.downloadPackIfNeedUpdate();
                });
            });
        });
    }
}
