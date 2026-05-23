package com.mega.map.client.shader.post;

import com.mega.endinglib.api.client.shader.post.CustomScreenEffect;
import com.mega.map.MegaMod;
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
        return ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "shaders/post/modern_gaussian_blur.json");
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
