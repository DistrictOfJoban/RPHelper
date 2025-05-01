package com.lx862.rphelper.command;

import com.lx862.rphelper.packet.ResourcePackSyncPacket;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class RPUpdateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("updateRP")
                .then(CommandManager.argument("players", EntityArgumentType.players())
                    .requires(ctx -> ctx.hasPermissionLevel(2))
                    .executes(ctx -> {
                        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(ctx, "players");
                        for(ServerPlayerEntity player : players) {
                            sendUpdateRequestToPlayer(player);
                        }
                        return 1;
                    })
                )
                .executes(ctx -> sendUpdateRequestToPlayer(ctx.getSource().getPlayerOrThrow()))
        );
    }

    private static int sendUpdateRequestToPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new ResourcePackSyncPacket(false));
        return 1;
    }
}
