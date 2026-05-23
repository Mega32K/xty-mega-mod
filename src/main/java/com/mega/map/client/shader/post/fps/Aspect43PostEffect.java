package com.mega.map.client.shader.post.fps;

import com.mega.endinglib.api.client.shader.post.CustomScreenEffect;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.common.data.DynamicEffectData;
import com.mega.map.MegaMod;
import com.mega.map.common.capability.FpsCapability;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.resources.ResourceLocation;

public class Aspect43PostEffect implements CustomScreenEffect {
    public static Aspect43PostEffect INSTANCE;

    public Aspect43PostEffect() {
        INSTANCE = this;
    }

    @Override
    public String getName() {
        return "aspect_4_3";
    }

    @Override
    public ResourceLocation getShaderLocation() {
        return ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "shaders/post/aspect_4_3.json");
    }

    @Override
    public void onRenderTick(float partialTicks) {
    }

    @Override
    public boolean canUse() {
        if (ClientWrapped.clientPlayer() == null) {
            return false;
        }
        return CommonProxy.getFPSCap(ClientWrapped.clientPlayer())
                .map(FpsCapability::isAspect43)
                .orElse(false);
    }

    @Override
    public DynamicEffectData.TransformLayer getTransformLayer() {
        return DynamicEffectData.TransformLayer.LEVEL_RENDERER;
    }

    @Override
    public boolean autoProcess() {
        return false;
    }
}
