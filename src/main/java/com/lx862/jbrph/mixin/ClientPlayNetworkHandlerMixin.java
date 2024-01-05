package com.lx862.jbrph.mixin;

import com.lx862.jbrph.data.manager.PackManager;
import com.lx862.jbrph.config.Config;
import com.lx862.jbrph.data.PackEntry;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onResourcePackSend", at = @At("HEAD"), cancellable = true)
    public void onResourcePackSend(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        for(PackEntry packEntry : Config.getPackEntries()) {
            if(packet.getURL().trim().equals(packEntry.sourceUrl.toString())) {
                if(PackManager.isPackReady(packEntry)) {
                    // Stop showing the vanilla server RP screen if we already have it downloaded
                    ci.cancel();
                }
            }
        }
    }
}