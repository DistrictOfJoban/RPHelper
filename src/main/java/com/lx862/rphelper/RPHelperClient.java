package com.lx862.rphelper;

import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.packet.Packets;
import net.fabricmc.api.ClientModInitializer;

public class RPHelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        Packets.registerClient();
    }
}
