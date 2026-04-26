package com.mega.xty.client.shader.post.map2;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.shader.post.TimePostEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class DeadPostEffect extends TimePostEffect {
    public static DeadPostEffect INSTANCE;

    public DeadPostEffect() {
        INSTANCE = this;
    }

    @Override
    public String getName() {
        return "dead";
    }

    @Override
    public void onRenderTick(float partialTicks) {
        super.onRenderTick(partialTicks);
    }

    @Override
    public ResourceLocation getShaderLocation() {
        return ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "shaders/post/dead.json");
    }

    @Override
    public boolean canUse() {
        this.canUse = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isShiftKeyDown();
        return super.canUse();
    }
}
