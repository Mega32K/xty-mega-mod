package com.mega.map.mixin.client.loading;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.map.client.overlay.loading.MegaStyleLoadingEffect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V", shift = At.Shift.AFTER))
    private void uiAnim(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {

    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"))
    private void wrapVertex(GuiGraphics instance, ResourceLocation tex, int x, int y, int renderWidth, int renderHeight, float startWidth, float startHeight, int endWidth, int endHeight, int resolutionX, int resolutionY, Operation<Void> original) {
        MegaStyleLoadingEffect.wrapVertex(((LoadingOverlay) (Object) this), instance, tex, x, y, renderWidth, renderHeight, startWidth ,startHeight, endWidth, endHeight, resolutionX, resolutionY, original);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;drawProgressBar(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V"))
    private void wrapProgress(LoadingOverlay instance, GuiGraphics p_283125_, int p_96184_, int p_96185_, int p_96186_, int p_96187_, float p_96188_, Operation<Void> original) {
        MegaStyleLoadingEffect.wrapProgress(instance, p_283125_, p_96184_, p_96185_, p_96186_, p_96187_, p_96188_, original);
    }
}
