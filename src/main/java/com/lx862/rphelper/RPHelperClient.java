package com.lx862.rphelper;

import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.data.manager.PackManager;
import com.lx862.rphelper.network.Packets;
import net.fabricmc.api.ClientModInitializer;

public class RPHelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        Packets.registerClient();
        PackManager.downloadOrUpdate(true);
    }
}
