package com.lx862.jbrph;

import com.lx862.jbrph.command.RPUpdateCommand;
import com.lx862.jbrph.network.Packets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RPHelper implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RPUpdateCommand.register(dispatcher);
        });
    }
}
