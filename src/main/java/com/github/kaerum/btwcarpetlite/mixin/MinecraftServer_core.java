package com.github.kaerum.btwcarpetlite.mixin;

import com.github.kaerum.btwcarpetlite.BTWCarpetLiteServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.WorldType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServer_core {
    @Inject(method = "loadAllWorlds", at = @At("HEAD"))
    private void serverLoad(String ignored, String ignored2, long ignored3, WorldType ignored4, String ignored5, CallbackInfo ci) {
        BTWCarpetLiteServer.onServerLoad((MinecraftServer)(Object)this);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void serverClose(CallbackInfo ci) {
        BTWCarpetLiteServer.onServerClose((MinecraftServer)(Object)this);
    }
}
