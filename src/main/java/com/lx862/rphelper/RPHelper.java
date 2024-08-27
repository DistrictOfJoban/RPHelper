package com.lx862.rphelper;

import com.lx862.rphelper.command.RPUpdateCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

public class RPHelper implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RPUpdateCommand.register(dispatcher);
        });
    }

    public static Identifier id(String path) {
        return Identifier.of("rphelper", path);
    }
}
