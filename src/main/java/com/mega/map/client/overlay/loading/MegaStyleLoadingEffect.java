package com.mega.map.client.overlay.loading;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.client.ClientContext;
import com.mega.endinglib.mixin.accessor.AccessorPostChain;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.endinglib.util.time.TimeContext;
import com.mega.map.MegaMod;
import com.mega.map.mixin.client.LoadingOverlayAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class MegaStyleLoadingEffect {
    @Nullable
    public static PostChain motionEffect;
    static ResourceLocation MOJANG = ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "textures/ui/mojangstudios.png");
    public static void wrapVertex(LoadingOverlay loadingOverlay, GuiGraphics instance, ResourceLocation tex, int x, int y, int renderWidth, int renderHeight, float startWidth, float startHeight, int endWidth, int endHeight, int resolutionX, int resolutionY, Operation<Void> original) {
        //1
        if (startWidth < 0) {
            long millisTime = Util.getMillis();
            float originalAlpha = RenderSystem.getShaderColor()[3];
            RenderSystem.setShaderColor(1F, 1F, 1F, originalAlpha * 0.5F);
            LoadingOverlayAccessor accessor = (LoadingOverlayAccessor) loadingOverlay;
            long escapedTime = millisTime - accessor.getFadeInStart();
            float fadeIn = Mth.clamp((millisTime - accessor.getFadeInStart()) / 500.0F, 0f, 1f);
            float fadeOut = Mth.clamp((millisTime - accessor.getFadeOutStart()) / 1000.0F, 0f, 1f);
            float timer_ = escapedTime < 500 ? fadeIn : (accessor.getFadeOutStart() > 0L && millisTime >= accessor.getFadeOutStart() ? 1F - fadeOut : 1.0F);
            if (accessor.getFadeInStart() < 0L)
                timer_ = 0;
            float fading = Easing.IN_OUT_EXPO.calculate(timer_);
            float yOffset = Math.max(20F, instance.guiHeight() * 0.1F);
            MegaGuiGraphics graphics = MegaGuiGraphics.of(instance);
            PoseStack poseStack = graphics.pose();

            poseStack.pushPose();
            float partialTime = TimeContext.Client.currentSeconds() % 2.5F;
            partialTime = partialTime < 1.5F ? Easing.IN_OUT_ELASTIC.interpolate(partialTime / 1.5F, 0F, Mth.PI * 2) : 0F;
            float iconSize = graphics.guiWidth() * 0.2F;
            poseStack.translate(graphics.guiWidth() * 0.5F, graphics.guiHeight() * 0.3F, 0);
            poseStack.mulPose(Axis.ZP.rotation(partialTime));
            RenderSystem.disableBlend();
            graphics.flush();
            if (originalAlpha < 1.0F) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }
            RenderSystem.setShaderColor(1F, 1F, 1F, originalAlpha * 2.0F);
            graphics.blit(ClientContext.UI_ICON_0, -iconSize*0.5F, -iconSize*0.55F, iconSize, iconSize, 0, 0, iconSize, iconSize, iconSize, iconSize);
            poseStack.popPose();
            try {
                if (motionEffect != null && !((AccessorPostChain) motionEffect).getPasses().isEmpty()) {
                    RenderSystem.disableDepthTest();
                    Minecraft mc = Minecraft.getInstance();
                    motionEffect.process(mc.getFrameTime());
                    mc.getMainRenderTarget().bindWrite(false);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            boolean isFadingOut = escapedTime > 500L && accessor.getFadeOutStart() > 0L && millisTime >= accessor.getFadeOutStart();
            poseStack.pushPose();
            poseStack.translate(x+renderWidth, y + fading * yOffset + renderHeight * 1.15F * 0.5F, 0);
            if (isFadingOut) {
                fading = 1f - fading;
                poseStack.mulPose(Axis.ZP.rotation(fading * Mth.PI * 2));
                poseStack.scale(1F - fading * 0.8F, 1F - fading * 0.8F, 1F - fading * 0.8F);
            }
            graphics.blit(MOJANG, -renderWidth, -renderHeight * 0.5F, renderWidth * 2F, renderHeight, 0, 0, 240, 60, 240, 60);
            poseStack.popPose();


        }
    }
    public static void wrapProgress(LoadingOverlay instance, GuiGraphics graphics, int x, int y, int endX, int endY, float alpha, Operation<Void> original) {
        LoadingOverlayAccessor accessor = (LoadingOverlayAccessor) instance;
        long millisTime = Util.getMillis();
        long escapedTime = millisTime - accessor.getFadeInStart();
        float fadeIn = Mth.clamp((millisTime - accessor.getFadeInStart()) / 500.0F, 0f, 1f);
        float fadeOut = Mth.clamp((millisTime - accessor.getFadeOutStart()) / 1000.0F, 0f, 1f);
        float timer_ = escapedTime < 500 ? fadeIn : (accessor.getFadeOutStart() > 0L && millisTime >= accessor.getFadeOutStart() ? 1F - fadeOut : 1.0F);
        if (accessor.getFadeInStart() < 0L)
            timer_ = 0;
        float fading = Easing.IN_OUT_EXPO.calculate(timer_);
        float yOffset = Math.max(20F, graphics.guiHeight() * 0.1F);
        drawProgressBar(accessor, MegaGuiGraphics.of(graphics), x, y + fading * yOffset, endX, endY + fading * yOffset, alpha);
    }
    private static void drawProgressBar(LoadingOverlayAccessor instance, MegaGuiGraphics guiGraphics, float x, float y, float endX, float endY, float alpha) {
        int i = Mth.ceil((float)(endX - x - 2) * instance.getCurrentProgress());
        int j = Math.round(alpha * 255.0F);
        int k = FastColor.ARGB32.color(j, 255, 255, 255);
        guiGraphics.fill(x + 2, y + 2, x + i, endY - 2, k);
        guiGraphics.fill(x + 1, y, endX - 1, y + 1, k);
        guiGraphics.fill(x + 1, endY, endX - 1, endY - 1, k);
        guiGraphics.fill(x, y, x + 1, endY, k);
        guiGraphics.fill(endX, y, endX - 1, endY, k);
    }
}
