package com.lx862.jbrph.network;

import com.lx862.jbrph.data.manager.PackManager;
import com.lx862.jbrph.data.manager.ServerLockManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class Packets {
    public static final Identifier VALIDATE_RP_STATE = new Identifier("jbrph", "validate_rp_state");
    public static final Identifier SEND_UPDATE_RP = new Identifier("jbrph", "rp_updated");

    public static void registerServer() {
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            ServerPlayNetworking.send(handler.player, VALIDATE_RP_STATE, buf);
        }));
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(VALIDATE_RP_STATE, (client, handler, buf, responseSender) -> {
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
