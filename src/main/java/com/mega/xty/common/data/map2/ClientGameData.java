package com.mega.xty.common.data.map2;

import com.google.common.collect.Queues;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.xty.common.event.map2.C4SpectateCameraHandler;
import com.mega.xty.common.event.map2.DeathCameraEffectHandler;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.util.FixedLengthList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientGameData {
    @javax.annotation.Nullable
    public static Entity pickedEntity = null;
    @javax.annotation.Nullable
    public static HitResult hitResult;
    public static boolean isStopped = true;
    public static boolean scoreOverlayVisible = false;
    public static Queue<DeathData> toAddDeathData = Queues.newArrayDeque();
    public static Queue<DeathData> toRemoveDeathData = Queues.newArrayDeque();
    public static FixedLengthList<DeathData> deathDataList = new FixedLengthList<>(4);
    public static int playerCountNeed = 0;
    public static boolean isTeamMode = false;
    public static int countdownMin;
    public static int countdownSec;
    public static int redTeamKillcount;
    public static int blueTeamKillcount;
    public static int redTeamScore;
    public static int blueTeamScore;
    public static boolean teamScoreVisible = false;
    public static int CFHealthTickCount = 0;
    @Nullable
    public static Component rightTopTextTip = null;
    @Nullable
    public static BlockPos pointA;
    @Nullable
    public static BlockPos pointB;
    public static String aText = "A";
    public static String bText = "B";
    public static int currentCameraPlayerIndex;
    public static List<AbstractClientPlayer> aliveSameTeamPlayers = new ObjectArrayList<>();
    public static List<AbstractClientPlayer> aliveSameTeamPlayersWithoutLocal = new ObjectArrayList<>();

    public static boolean shouldRenderCFHealth() {
        return !ClientGameData.isStopped && (!ClientGame1Data.isStopped || !ClientGame2Data.isStopped);
    }

    public static boolean map2Playing() {
        return !ClientGameData.isStopped && (!ClientGame1Data.isStopped || !ClientGame2Data.isStopped);
    }

    public static void tick(ClientLevel clientLevel) {
        Player player = ClientWrapped.clientPlayer();
        if (clientLevel != null && player != null) {
            aText = Component.translatable("game_2.point.a").getString();
            bText = Component.translatable("game_2.point.b").getString();
            ClientGame1Data.tick(clientLevel);
            if (ClientGameData.isStopped) {
                if (!shouldRenderCFHealth())
                    CFHealthTickCount = 0;
                return;
            } else {
                if (shouldRenderCFHealth())
                    CFHealthTickCount++;
                else CFHealthTickCount = 0;
            }
            aliveSameTeamPlayers.clear();
            aliveSameTeamPlayersWithoutLocal.clear();
            AtomicInteger red = new AtomicInteger(0);
            AtomicInteger blue = new AtomicInteger(0);
            for (AbstractClientPlayer p : clientLevel.players()) {
                if (p.getTeam() != null) {
                    if (p.isAlliedTo(player)) {
                        if (p != player)
                            aliveSameTeamPlayersWithoutLocal.add(p);
                        aliveSameTeamPlayers.add(p);
                    }
                    if (p.getTeamColor() == ChatFormatting.RED.getColor()) {
                        CommonProxy.getMap2Cap(p).ifPresent(map2Capability -> red.addAndGet(map2Capability.get1KillCount()));
                    } else if (p.getTeamColor() == ChatFormatting.BLUE.getColor()) {
                        CommonProxy.getMap2Cap(p).ifPresent(map2Capability -> blue.addAndGet(map2Capability.get1KillCount()));
                    }
                }
            }
            redTeamKillcount = red.get();
            blueTeamKillcount = blue.get();
            refreshDeadSpectateTarget(player);
        }
    }

    public void tick() {
    }

    public static void updateAliveSameTeamPlayers() {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        aliveSameTeamPlayers.clear();
        aliveSameTeamPlayersWithoutLocal.clear();
        for (AbstractClientPlayer p : clientLevel.players()) {
            if (p.getTeam() != null) {
                if (p.isAlliedTo(player)) {
                    if (p != player)
                        aliveSameTeamPlayersWithoutLocal.add(p);
                    aliveSameTeamPlayers.add(p);
                }
            }
        }
    }

    public static void fpsSpectate() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (DeathCameraEffectHandler.isPlaying()) return;
        updateAliveSameTeamPlayers();
        List<AbstractClientPlayer> spectatablePlayers = getSpectatablePlayers();
        int pIndex = ClientGameData.currentCameraPlayerIndex;
        if (pIndex >= 0 && !spectatablePlayers.isEmpty() && spectatablePlayers.size() > pIndex) {
            C4SpectateCameraHandler.stop();
            mc.setCameraEntity(spectatablePlayers.get(pIndex));
        } else {
            mc.setCameraEntity(player);
            var capOpt = CommonProxy.getMap2Cap(player);
            if (!capOpt.isPresent()) {
                C4SpectateCameraHandler.stop();
                return;
            }
            capOpt.ifPresent(cap -> {
                if (cap.isXaeroDead() && ClientGame2Data.playing()) {
                    if (cap.getPlayerC4Pos().isPresent()) {
                        cap.getPlayerC4Pos().ifPresent(C4SpectateCameraHandler::start);
                    } else {
                        C4SpectateCameraHandler.stop();
                        getRandomEnemySpectatablePlayer(player).ifPresentOrElse(mc::setCameraEntity, () ->
                                getNearestPointSpectatePos(player).ifPresent(pos ->
                                        C4SpectateCameraHandler.start(pos, getNearestPointCenter(player).orElse(null))
                                )
                        );
                    }
                } else {
                    C4SpectateCameraHandler.stop();
                }
            });
        }
    }

    public static void hardCutDeadSpectateTarget(Entity deadEntity) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || deadEntity == null || mc.getCameraEntity() != deadEntity) {
            return;
        }
        if (DeathCameraEffectHandler.isPlaying() || !ClientGame2Data.playing()) {
            return;
        }
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            if (!cap.isXaeroDead()) {
                return;
            }
            mc.setCameraEntity(player);
            C4SpectateCameraHandler.stop();
            currentCameraPlayerIndex = -1;
            fpsSpectate();
        });
    }

    private static void refreshDeadSpectateTarget(Player player) {
        if (DeathCameraEffectHandler.isPlaying()) {
            return;
        }
        if (!ClientGame2Data.playing()) {
            return;
        }
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            if (!cap.isXaeroDead()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            List<AbstractClientPlayer> sameTeamTargets = getSpectatablePlayers();
            Entity cameraEntity = mc.getCameraEntity();
            if (!(cameraEntity instanceof AbstractClientPlayer cameraPlayer) || cameraPlayer == player) {
                if (!sameTeamTargets.isEmpty()) {
                    currentCameraPlayerIndex = 0;
                    fpsSpectate();
                    return;
                }
                if (C4SpectateCameraHandler.isActive()) {
                    if (cap.getPlayerC4Pos().isPresent()
                            || !(player instanceof LocalPlayer localPlayer)
                            || getRandomEnemySpectatablePlayer(localPlayer).isEmpty()) {
                        return;
                    }
                }
                fpsSpectate();
                return;
            }
            boolean currentTargetDead = CommonProxy.getMap2Cap(cameraPlayer).map(map2Capability -> map2Capability.isXaeroDead()).orElse(false);
            if (currentTargetDead) {
                currentCameraPlayerIndex = sameTeamTargets.isEmpty() ? -1 : 0;
                fpsSpectate();
            } else if (cameraPlayer.isAlliedTo(player)) {
                if (!sameTeamTargets.contains(cameraPlayer)) {
                    currentCameraPlayerIndex = sameTeamTargets.isEmpty() ? -1 : 0;
                    fpsSpectate();
                }
            } else if (!sameTeamTargets.isEmpty()) {
                currentCameraPlayerIndex = 0;
                fpsSpectate();
            }
        });
    }

    public static List<AbstractClientPlayer> getSpectatablePlayers() {
        List<AbstractClientPlayer> spectatablePlayers = new ArrayList<>();
        for (AbstractClientPlayer candidate : aliveSameTeamPlayersWithoutLocal) {
            boolean isDead = CommonProxy.getMap2Cap(candidate).map(map2Capability -> map2Capability.isXaeroDead()).orElse(false);
            if (!isDead) {
                spectatablePlayers.add(candidate);
            }
        }
        return spectatablePlayers;
    }

    public static Optional<AbstractClientPlayer> getRandomEnemySpectatablePlayer(LocalPlayer localPlayer) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) {
            return Optional.empty();
        }
        List<AbstractClientPlayer> spectatablePlayers = new ArrayList<>();
        for (AbstractClientPlayer candidate : clientLevel.players()) {
            if (candidate == localPlayer || candidate.isAlliedTo(localPlayer)) {
                continue;
            }
            boolean isDead = CommonProxy.getMap2Cap(candidate).map(map2Capability -> map2Capability.isXaeroDead()).orElse(false);
            if (!isDead) {
                spectatablePlayers.add(candidate);
            }
        }
        if (spectatablePlayers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(spectatablePlayers.get(ThreadLocalRandom.current().nextInt(spectatablePlayers.size())));
    }

    public static Optional<Vec3> getNearestPointCenter(LocalPlayer localPlayer) {
        return CommonProxy.getMap2Cap(localPlayer).map(cap -> {
            Vec3 deathPos = cap.getLastDeathPos().map(Vec3::new).orElse(localPlayer.position());
            BlockPos nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (BlockPos point : List.of(pointA, pointB)) {
                if (point == null) {
                    continue;
                }
                double dx = Vec3.atCenterOf(point).x - deathPos.x;
                double dz = Vec3.atCenterOf(point).z - deathPos.z;
                double distance = dx * dx + dz * dz;
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = point;
                }
            }
            return nearest == null ? Optional.<Vec3>empty() : Optional.of(Vec3.atCenterOf(nearest));
        }).orElseGet(Optional::empty);
    }

    public static Optional<Vec3> getNearestPointSpectatePos(LocalPlayer localPlayer) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return Optional.empty();
        }
        return getNearestPointCenter(localPlayer).map(center -> {
            Vec3[] candidates = new Vec3[] {
                    center.add(0.0D, 1.5D, 4.0D),
                    center.add(0.0D, 1.5D, -4.0D),
                    center.add(4.0D, 1.5D, 0.0D),
                    center.add(-4.0D, 1.5D, 0.0D),
                    center.add(0.0D, 2.5D, 0.0D)
            };
            for (Vec3 candidate : candidates) {
                if (level.noCollision(localPlayer, localPlayer.getBoundingBox().move(candidate.subtract(localPlayer.position())))) {
                    return candidate;
                }
            }
            return center.add(0.0D, 2.5D, 0.0D);
        });
    }
}
