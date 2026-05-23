package com.mega.map.mixin.xaero;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.map.common.data.map2.ClientGameData;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.hud.Hud;
import xaero.hud.render.HudRenderer;

@Mixin(value = HudRenderer.class, remap = false)
@ModDependsMixin("xaerominimap")
public class HudRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(Hud hud, GuiGraphics guiGraphics, float partialTicks, CallbackInfo ci) {
        if (!ClientGameData.map2Playing()) ci.cancel();
    }
}
