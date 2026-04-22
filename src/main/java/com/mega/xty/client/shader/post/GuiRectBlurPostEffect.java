package com.mega.xty.client.shader.post;

import com.mega.endinglib.api.client.shader.post.CustomScreenEffect;
import com.mega.endinglib.client.renderer.shader.post.ModernGaussianBlurPostEffect;
import com.mega.endinglib.util.SafeClass;
import com.mega.xty.XtyMegaMod;
import net.minecraft.resources.ResourceLocation;

public class GuiRectBlurPostEffect implements CustomScreenEffect {
    public static GuiRectBlurPostEffect INSTANCE;

    public GuiRectBlurPostEffect() {
        INSTANCE = this;
    }

    @Override
    public String getName() {
        return "modern_gaussian_blur";
    }

    @Override
    public ResourceLocation getShaderLocation() {
        return ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "shaders/post/modern_gaussian_blur.json");
    }

    @Override
    public void onRenderTick(float partialTicks) {
    }

    @Override
    public boolean canUse() {
        return false;
    }

    @Override
    public boolean autoProcess() {
        return false;
    }
}
