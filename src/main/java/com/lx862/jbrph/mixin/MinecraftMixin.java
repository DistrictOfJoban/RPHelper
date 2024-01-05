package com.lx862.jbrph.mixin;

import com.lx862.jbrph.ClientEventQueue;
import com.lx862.jbrph.data.manager.ServerLockManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MinecraftMixin {
    @Inject(method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
    public void reloadResources(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ServerLockManager.updatePackState(true);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;createResourcePacks()Ljava/util/List;"))
    public void reloadResources(RunArgs args, CallbackInfo ci) {
//        ClientEventQueue.invokeRPLoad();
        ServerLockManager.updatePackState(true);
    }
}
