package com.lx862.jbrph;

import com.lx862.jbrph.config.Config;
import com.lx862.jbrph.data.manager.PackManager;
import com.lx862.jbrph.network.Packets;
import net.fabricmc.api.ClientModInitializer;

public class RPHelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        Packets.registerClient();
        PackManager.downloadPackIfNeedUpdate();
    }
}
