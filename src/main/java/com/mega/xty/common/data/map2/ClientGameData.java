package com.mega.xty.common.data.map2;

import com.google.common.collect.Queues;
import com.mega.endinglib.client.ClientWrapped;
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
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientGameData {
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
                    //noinspection DataFlowIssue
                    if (p.getTeamColor() == ChatFormatting.RED.getColor()) {
                        CommonProxy.getMap2Cap(p).ifPresent(map2Capability -> {
                            red.addAndGet(map2Capability.get1KillCount());
                        });
                    } else //noinspection DataFlowIssue
                        if (p.getTeamColor() == ChatFormatting.BLUE.getColor()) {
                            CommonProxy.getMap2Cap(p).ifPresent(map2Capability -> {
                                blue.addAndGet(map2Capability.get1KillCount());
                            });
                        }
                }
            }
            redTeamKillcount = red.get();
            blueTeamKillcount = blue.get();
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
        updateAliveSameTeamPlayers();
        int pIndex = ClientGameData.currentCameraPlayerIndex;
        if (pIndex >= 0 && !aliveSameTeamPlayersWithoutLocal.isEmpty() && aliveSameTeamPlayersWithoutLocal.size() > pIndex) {
            mc.setCameraEntity(aliveSameTeamPlayersWithoutLocal.get(pIndex));
        } else {
            mc.setCameraEntity(player);
        }
    }
}
