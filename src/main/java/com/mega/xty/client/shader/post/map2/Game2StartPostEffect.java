package com.mega.xty.client.shader.post.map2;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.shader.post.TimePostEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class Game2StartPostEffect extends TimePostEffect {
    public static Game2StartPostEffect INSTANCE;

    public Game2StartPostEffect() {
        INSTANCE = this;
    }

    @Override
    public String getName() {
        return "game2_aqua";
    }

    @Override
    public ResourceLocation getShaderLocation() {
        return ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "shaders/post/game2_aqua.json");
    }

    @Override
    public boolean canUse() {
        this.canUse = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isShiftKeyDown();
        return super.canUse();
    }
}
