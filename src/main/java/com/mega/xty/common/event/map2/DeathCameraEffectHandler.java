package com.mega.xty.common.event.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.event.render.CameraPosEvent;
import com.mega.xty.client.shader.post.map2.DeadPostEffect;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeathCameraEffectHandler {
    private static final int EFFECT_TICKS = 60;
    private static final int MOVE_TICKS = 20;
    private static final float LOOK_DOWN_X_ROT = 60F;
    private static final float ROLL_MIN = 20F;
    private static final float ROLL_MAX = 30F;
    private static boolean playing;
    private static int tickCount;
    private static Vec3 start = Vec3.ZERO;
    private static Vec3 end = Vec3.ZERO;
    private static float xRot;
    private static float yRot;
    private static float zRot;

    public static void play(Vec3 start, Vec3 end, float xRot, float yRot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        if (!CommonProxy.getFPSCap(minecraft.player).map(cap -> cap.getGame2ClientOptions().visual().useDeathCamera()).orElse(true)) {
            if (CommonProxy.getFPSCap(minecraft.player).map(cap -> cap.getGame2ClientOptions().visual().useDeadPostEffect()).orElse(true)) {
                DeadPostEffect.start(EFFECT_TICKS);
            }
            return;
        }
        if (playing) {
            DeadPostEffect.stop();
        }
        playing = true;
        tickCount = 0;
        DeathCameraEffectHandler.start = start;
        DeathCameraEffectHandler.end = end;
        DeathCameraEffectHandler.xRot = xRot;
        DeathCameraEffectHandler.yRot = yRot;
        DeathCameraEffectHandler.zRot = randomDeathRoll();
        DeadPostEffect.start(EFFECT_TICKS);
    }

    public static void clientTick() {
        DeadPostEffect.clientTick();
        if (!playing) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            stop();
            return;
        }
        tickCount++;
        if (tickCount >= EFFECT_TICKS) {
            stop();
        }
    }

    public static void stop() {
        DeadPostEffect.stop();
        playing = false;
        tickCount = 0;
        start = Vec3.ZERO;
        end = Vec3.ZERO;
        xRot = 0F;
        yRot = 0F;
        zRot = 0F;
    }

    public static boolean isPlaying() {
        return playing;
    }

    @SubscribeEvent
    public static void onCameraPos(CameraPosEvent.Pre event) {
        if (!playing) return;
        float progress = Mth.clamp((tickCount + (float) event.getPartialTick()) / (float) MOVE_TICKS, 0F, 1F);
        float eased = Easing.OUT_CUBIC.calculate(progress);
        event.setX(Mth.lerp(eased, start.x, end.x));
        event.setY(Mth.lerp(eased, start.y, end.y));
        event.setZ(Mth.lerp(eased, start.z, end.z));
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!playing) return;
        float progress = Mth.clamp((tickCount + (float) event.getPartialTick()) / (float) MOVE_TICKS, 0F, 1F);
        float eased = Easing.OUT_SINE.calculate(progress);
        float targetXRot = Mth.clamp(Math.max(xRot, LOOK_DOWN_X_ROT), -90F, 90F);
        event.setYaw(yRot);
        event.setPitch(Mth.lerp(eased, xRot, targetXRot));
        event.setRoll(Mth.lerp(eased, 0F, zRot));
    }

    private static float randomDeathRoll() {
        float sign = Math.random() < 0.5D ? -1F : 1F;
        return sign * (ROLL_MIN + (float) Math.random() * (ROLL_MAX - ROLL_MIN));
    }
}
