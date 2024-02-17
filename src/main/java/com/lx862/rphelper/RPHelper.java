package com.lx862.rphelper;

import com.lx862.rphelper.command.RPUpdateCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class RPHelper implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RPUpdateCommand.register(dispatcher);
        });
    }
}
