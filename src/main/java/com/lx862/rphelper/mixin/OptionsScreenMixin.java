package com.lx862.rphelper.mixin;

import com.lx862.rphelper.data.manager.ServerLockManager;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.resource.ResourcePackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {
    @Inject(method = "refreshResourcePacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;write()V"))
    public void refreshResourcePacks(ResourcePackManager resourcePackManager, CallbackInfo ci) {
        ServerLockManager.updatePackState(true);
    }
}
