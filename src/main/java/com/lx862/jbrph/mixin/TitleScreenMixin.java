package com.lx862.jbrph.mixin;

import com.lx862.jbrph.data.manager.PackManager;
import com.lx862.jbrph.data.manager.ToastManager;
import com.lx862.jbrph.config.Config;
import com.lx862.jbrph.data.PackEntry;
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

            if(PackManager.stillDownloading) {
                for(PackEntry packEntry : Config.getPackEntries()) {
                    if(!PackManager.isPackReady(packEntry)) {
                        ToastManager.setupNewDownloadToast(packEntry);
                    }
                }
            }
            toastShown = true;
        }
    }
}
