package com.lx862.rphelper;

import com.lx862.rphelper.command.RPUpdateCommand;
import com.lx862.rphelper.packet.ResourcePackSyncPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;

public class RPHelper implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RPUpdateCommand.register(dispatcher);
        });
        PayloadTypeRegistry.playS2C().register(ResourcePackSyncPacket.ID, ResourcePackSyncPacket.CODEC);
    }

    public static Identifier id(String path) {
        return Identifier.of("rphelper", path);
    }
}
