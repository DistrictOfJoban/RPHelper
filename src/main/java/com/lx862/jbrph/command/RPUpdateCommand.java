package com.lx862.jbrph.command;

import com.lx862.jbrph.network.Packets;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class RPUpdateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("updateRP")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    PacketByteBuf buf = PacketByteBufs.create();
                    ServerPlayNetworking.send(player, Packets.SEND_UPDATE_RP, buf);
                    return 1;
                })
        );
    }
}
