package com.mega.map.mixin.client;

import com.mega.map.common.item.IDebugItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Nullable public LocalPlayer player;

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void cancelPickBlockWhenHandheldDebugItem(CallbackInfo ci) {
        if (this.player != null) {
            if (this.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IDebugItem) {
                ci.cancel();
            }
        }
    }
}
