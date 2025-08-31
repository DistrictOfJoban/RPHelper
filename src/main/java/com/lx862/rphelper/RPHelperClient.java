package com.lx862.rphelper;

import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.data.manager.PackManager;
import com.lx862.rphelper.network.Packets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RPHelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        Packets.registerClient();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("rphelper")
                            .then(ClientCommandManager.literal("reload")
                                    .executes(context -> {
                                        context.getSource().sendFeedback(Text.literal("[RPHelper] Initiating download for missing/outdated resource packs.").formatted(Formatting.YELLOW));
                                        context.getSource().sendFeedback(Text.literal("[RPHelper] " + Config.getPackEntries().size() + " pack(s) registered in config.").formatted(Formatting.YELLOW));
                                        context.getSource().sendFeedback(Text.literal("[RPHelper] For detailed progress, please monitor the console log.").formatted(Formatting.YELLOW));
                                        PackManager.downloadOrUpdate(false);
                                        return 1;
                                })
                            )
                    );
        });
    }
}
