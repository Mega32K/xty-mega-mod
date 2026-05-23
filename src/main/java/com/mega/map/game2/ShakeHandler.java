package com.mega.map.game2;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import com.mega.endinglib.api.event.render.CameraPosEvent;
import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.map.common.data.map1.ClientGame2Data;
import com.mega.map.common.network.s2c.map1.game2.S2CScreenShakePacket;
import com.mega.map.common.network.s2c.map1.game2.S2CSimpleScreenShakePacket;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Iterator;
import java.util.Queue;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShakeHandler {
    public static final Queue<ShakeInstance> toAdd = Queues.newArrayDeque();
    public static final Queue<ShakeInstance> shakes = EvictingQueue.create(32);
    public static void shake(float power, Vec2 arrow, int duration, int circles) {
        toAdd.add(new ShakeInstance(power, duration, 0, arrow, circles));
    }
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.START) {
            if (!shakes.isEmpty()) {
                Iterator<ShakeInstance> iterator = shakes.iterator();
                while(iterator.hasNext()) {
                    ShakeInstance shakeInstance = iterator.next();
                    shakeInstance.tick();
                    if (shakeInstance.removed) {
                        iterator.remove();
                    }
                }
            }
            if (!toAdd.isEmpty()) {
                ShakeInstance shakeInstance;
                while((shakeInstance = toAdd.poll()) != null) {
                    shakes.add(shakeInstance);
                }
            }
            if (mc.level == null || mc.player == null)
                shakes.clear();
        }
    }
    @SubscribeEvent
    public static void cameraSetup(CameraPosEvent.Post event) {
        if (!shakes.isEmpty()) {
            float xDelta = 0;
            float yDelta = 0;
            for (ShakeInstance shakeInstance : shakes) {
                xDelta += shakeInstance.getX((float) event.getPartialTick());
                yDelta += shakeInstance.getY((float) event.getPartialTick());
            }
            xDelta = Mth.clamp(xDelta, -1.5F, 1.5F);
            yDelta = Mth.clamp(yDelta, -1.5F, 1.5F);
            if (ClientGame2Data.blackHoleExist || (Util.getMillis() - ClientGame2Data.blackDisappearTime) < 1500) {
                float time = (Util.getMillis() - ClientGame2Data.blackAppearTime) / 500F;
                float progress = Mth.clamp(time / 3F, 0, 1F);
                if (!ClientGame2Data.blackHoleExist) {
                    progress = Mth.clamp(1F - ((Util.getMillis() - ClientGame2Data.blackDisappearTime) / 1500F), 0F, 1F);
                }
                xDelta += progress * (Mth.cos(time * Mth.PI * 1.5F) * 0.05F);
                yDelta += progress * (-Mth.sin(time * Mth.PI * 1.5F) * 0.05F);
            }
            CameraType cameraType = Minecraft.getInstance().options.getCameraType();
            if (cameraType == CameraType.THIRD_PERSON_BACK) {
                yDelta *= -1.0F;
                xDelta *= -1.0F;
            } else {
                xDelta *= -1.0F;
            }
            event.move(0, yDelta, xDelta);
        }
    }
    public static void handleSimpleScreenShake(S2CSimpleScreenShakePacket packet) {
        ShakeHandler.shake(packet.getPower(), new Vec2(packet.getX(), packet.getY()), 8, 2);
    }
    public static void handleScreenShake(S2CScreenShakePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vector3f start = packet.getStart().add(camera.getPosition().toVector3f().mul(-1F).add(0.5F, 0.F, 0.5F));
        Vector3f end = packet.getEnd().add(camera.getPosition().toVector3f().mul(-1F).add(0.5F, 0.F, 0.5F));

        Matrix4f matrix = new Matrix4f(ClientUtils.LEVEL_PROJ_MAT).mul(ClientUtils.LEVEL_MODEL_VIEW_MAT);
        {
            Vec2 start2d;
            Vec2 end2d;
            Vector4f v4 = matrix.transform(new Vector4f(start, 0F));
            start2d = new Vec2((v4.x/v4.z+1)/2f, (v4.y/v4.z+1)/2f);
            v4 = matrix.transform(new Vector4f(end, 0F));
            end2d = new Vec2((v4.x/v4.z+1)/2f, (v4.y/v4.z+1)/2f);
            ShakeHandler.shake(packet.getPower(), start2d.add(end2d.scale(-1F)), 12, 6);
        }
    }
}
