package com.lx862.rphelper.mixin;

import com.lx862.rphelper.data.manager.PackManager;
import com.lx862.rphelper.data.manager.PackApplicationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;createResourcePacks()Ljava/util/List;"))
    public void reloadResources(RunArgs args, CallbackInfo ci) {
        PackManager.downloadOrUpdate(true);
        PackApplicationManager.updatePackState(true);
    }
}
