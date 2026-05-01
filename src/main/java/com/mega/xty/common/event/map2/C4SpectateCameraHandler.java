package com.mega.xty.common.event.map2;

import com.mega.endinglib.api.event.render.CameraPosEvent;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.entity.C4Entity;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class C4SpectateCameraHandler {
    private static final double C4_TARGET_SEARCH_RADIUS = 64.0D;
    private static boolean active;
    private static Vec3 cameraPos = Vec3.ZERO;
    @Nullable
    private static Vec3 lookTarget;

    public static void start(Vector3f pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        active = true;
        cameraPos = new Vec3(pos);
        lookTarget = findLookTarget(minecraft.level, cameraPos);
        faceLookTarget(minecraft.player);
    }

    public static void stop() {
        active = false;
        cameraPos = Vec3.ZERO;
        lookTarget = null;
    }

    public static boolean isActive() {
        return active;
    }

    public static void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null) {
            stop();
            return;
        }
        if (!ClientGame2Data.playing()) {
            stop();
            return;
        }
        var capOpt = CommonProxy.getMap2Cap(player);
        if (!capOpt.isPresent()) {
            stop();
            return;
        }
        capOpt.ifPresent(cap -> {
            if (!cap.isXaeroDead()) {
                stop();
                return;
            }
            cap.getPlayerC4Pos().ifPresentOrElse(pos -> {
                cameraPos = new Vec3(pos);
                active = true;
            }, C4SpectateCameraHandler::stop);
        });
    }

    @SubscribeEvent
    public static void onCameraPos(CameraPosEvent.Pre event) {
        if (!active) return;
        refresh();
        if (!active) return;
        event.setX(cameraPos.x);
        event.setY(cameraPos.y);
        event.setZ(cameraPos.z);
    }

    private static void faceLookTarget(LocalPlayer player) {
        if (!active || lookTarget == null) return;
        Vec3 from = cameraPos;
        Vec3 target = lookTarget;
        double dx = target.x - from.x;
        double dy = target.y - from.y;
        double dz = target.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Mth.atan2(dy, horizontal) * 180.0D / Math.PI));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        player.setYHeadRot(yaw);
        player.setYBodyRot(yaw);
    }

    @Nullable
    private static Vec3 findLookTarget(ClientLevel level, Vec3 spectatePos) {
        List<C4Entity> nearbyC4 = level.getEntitiesOfClass(C4Entity.class, new AABB(spectatePos, spectatePos).inflate(C4_TARGET_SEARCH_RADIUS));
        if (!nearbyC4.isEmpty()) {
            return nearbyC4.stream()
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(spectatePos)))
                    .map(Entity::position)
                    .map(pos -> pos.add(0.0D, 0.2D, 0.0D))
                    .orElse(null);
        }

        BlockPos[] sitePoints = {ClientGameData.pointA, ClientGameData.pointB};
        for (BlockPos sitePoint : sitePoints) {
            if (sitePoint != null) {
                return Vec3.atCenterOf(sitePoint);
            }
        }
        return null;
    }
}
