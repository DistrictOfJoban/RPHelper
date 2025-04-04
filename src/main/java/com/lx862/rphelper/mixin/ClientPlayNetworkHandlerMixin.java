package com.lx862.rphelper.mixin;

import com.lx862.rphelper.config.Config;
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
        // Prevent server resource pack prompt if the same is already loaded via RPHelper
        if(Config.havePackEntryWithUrl(packet.getURL())) {
            ci.cancel();
        }
    }
}
