package com.mega.xty.client.shader.post.map2;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.shader.post.TimePostEffect;
import com.mega.xty.common.data.map2.Map2Config;
import net.minecraft.resources.ResourceLocation;

public class MotionBlurPostEffect extends TimePostEffect {
    public static MotionBlurPostEffect INSTANCE;

    public MotionBlurPostEffect() {
        INSTANCE = this;
    }

    @Override
    public String getName() {
        return "motion_blur";
    }

    @Override
    public ResourceLocation getShaderLocation() {
        return ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "shaders/post/motion_blur.json");
    }

    @Override
    public boolean canUse() {
        return Map2Config.motion_blur;
    }
}
