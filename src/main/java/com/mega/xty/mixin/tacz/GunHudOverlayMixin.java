package com.mega.xty.mixin.tacz;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.xty.common.data.map2.ClientGameData;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ModDependsMixin("tacz")
@Mixin(value = GunHudOverlay.class, remap = false)
public abstract class GunHudOverlayMixin {
    @Unique
    private static final float TACZ_GUN_HUD_WIDTH = 117.0F;
    @Unique
    private static final float TACZ_GUN_HUD_BOTTOM_OFFSET = 22.0F;

    @Inject(method = "render", at = @At("HEAD"))
    private void xty$centerHudOnX(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci) {
        if (!ClientGameData.map2Playing()) return;
        graphics.pose().pushPose();
        graphics.pose().translate((TACZ_GUN_HUD_WIDTH - screenWidth) / 2.0F, TACZ_GUN_HUD_BOTTOM_OFFSET, 0.0F);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void xty$restoreHudPose(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci) {
        if (!ClientGameData.map2Playing()) return;
        graphics.pose().popPose();
    }
}
