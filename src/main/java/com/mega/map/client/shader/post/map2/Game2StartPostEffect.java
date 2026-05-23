package com.mega.map.client.shader.post.map2;

import com.mega.map.MegaMod;
import com.mega.map.client.shader.post.TimePostEffect;
import net.minecraft.resources.ResourceLocation;

public class Game2StartPostEffect extends TimePostEffect {
    public static Game2StartPostEffect INSTANCE;
    private int remainingTicks;

    public Game2StartPostEffect() {
        INSTANCE = this;
    }

    @Override
    public String getName() {
        return "game2_aqua";
    }

    @Override
    public ResourceLocation getShaderLocation() {
        return ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "shaders/post/game2_aqua.json");
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    public static void start(int durationTicks) {
        if (INSTANCE != null) {
            INSTANCE.canUse = true;
            INSTANCE.remainingTicks = Math.max(1, durationTicks);
            INSTANCE.time = 0F;
            INSTANCE.lastStamp = 0F;
        }
    }

    public static void stop() {
        if (INSTANCE != null) {
            INSTANCE.canUse = false;
            INSTANCE.remainingTicks = 0;
            INSTANCE.time = 0F;
            INSTANCE.lastStamp = 0F;
        }
    }

    public static void clientTick() {
        if (INSTANCE != null && INSTANCE.canUse && INSTANCE.remainingTicks > 0) {
            INSTANCE.remainingTicks--;
            if (INSTANCE.remainingTicks <= 0) {
                stop();
            }
        }
    }
}
