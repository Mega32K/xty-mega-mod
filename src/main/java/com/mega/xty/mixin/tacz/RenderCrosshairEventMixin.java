package com.mega.xty.mixin.tacz;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mojang.blaze3d.platform.Window;
import com.tacz.guns.client.event.RenderCrosshairEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ModDependsMixin("tacz")
@Mixin(value = RenderCrosshairEvent.class, remap = false)
public abstract class RenderCrosshairEventMixin {
    @Inject(method = "renderHitMarker", at = @At("HEAD"), cancellable = true)
    private static void xty$hideHitMarkerInGame2(GuiGraphics graphics, Window window, CallbackInfo ci) {
        if (ClientGame2Data.playing() && isSurvivalOrAdventure()) {
            ci.cancel();
        }
    }

    private static boolean isSurvivalOrAdventure() {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) {
            return false;
        }
        GameType playerMode = gameMode.getPlayerMode();
        return playerMode == GameType.SURVIVAL || playerMode == GameType.ADVENTURE;
    }
}
