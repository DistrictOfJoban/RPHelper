package com.lx862.rphelper.mixin;

import com.lx862.rphelper.data.manager.PackManager;
import com.lx862.rphelper.data.manager.ToastManager;
import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.data.PackEntry;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Unique
    private static boolean toastShown = false;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(CallbackInfo ci) {
        if(!toastShown) {
            ToastManager.readyToSendToast();
            for(PackEntry packEntry : Config.getPackEntries()) {
                if(!packEntry.ready) {
                    ToastManager.setupNewDownloadToast(packEntry);
                }
            }
            toastShown = true;
        }
    }
}
