package com.lx862.jbrph;

import com.lx862.jbrph.config.Config;
import com.lx862.jbrph.data.manager.PackManager;
import com.lx862.jbrph.network.Packets;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RPHelperClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("RPHelperClient");
    @Override
    public void onInitializeClient() {
        Config.load();
        Packets.registerClient();
        PackManager.downloadPackIfNeedUpdate();
    }
}
