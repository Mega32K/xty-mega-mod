package com.mega.xty.common.data.fps;

import com.mega.xty.common.entity.C4Entity;
import com.mega.xty.common.init.SoundsInit;
import com.mega.xty.proxy.ClientProxy;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.data.fps.kad.SynchedKADData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientFpsData {
    public static final int BOMB_COUNTDOWN_TOTAL_TICKS = 40 * 20;
    public static final int BOMB_COUNTDOWN_REQUEST_INTERVAL = 15 * 20;
    public static final int BOMB_COUNTDOWN_PROMPT_DURATION = 60;
    public static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.<PlayerInfo>comparingInt(pInfo -> getPlayerKAD(pInfo).getOrDefaultKAD(KAD.KAD_GENERAL).kills).thenComparing(pInfo -> getPlayerKAD(pInfo).getOrDefaultKAD(KAD.KAD_GENERAL).assists).thenComparing(pInfo -> getPlayerKAD(pInfo).getOrDefaultKAD(KAD.KAD_GENERAL).deaths);
    public static boolean enabled;
    public static final Map<UUID, SynchedKADData> kadData = new Object2ObjectOpenHashMap<>();
    public static final Map<UUID, TabData> playerDisplayNames = new Object2ObjectOpenHashMap<>();
    public static int bombPlantedTickCount;
    public static boolean bombExist;
    public static String bombPosition;
    public static int bombCountdownTicks;
    public static int bombCountdownRenderTicks;
    public static int bombCountdownRenderTimer;
    public static SynchedKADData getPlayerKAD(Player player) {
        if (!enabled) return SynchedKADData.EMPTY_KAD;
        return kadData.getOrDefault(player.getUUID(), SynchedKADData.EMPTY_KAD);
    }
    public static SynchedKADData getPlayerKAD(PlayerInfo playerInfo) {
        if (!enabled) return SynchedKADData.EMPTY_KAD;
        return kadData.getOrDefault(playerInfo.getProfile().getId(), SynchedKADData.EMPTY_KAD);
    }
    public static Component getPlayerName(PlayerInfo playerInfo) {
        return playerDisplayNames.getOrDefault(playerInfo.getProfile().getId(), new TabData(false, getNameForDisplay(playerInfo))).component;
    }
    public static boolean getPlayerTabDead(PlayerInfo playerInfo) {
        return playerDisplayNames.getOrDefault(playerInfo.getProfile().getId(), new TabData(false, Component.literal(""))).isDead;
    }
    public static Component getNameForDisplay(PlayerInfo p_94550_) {
        return p_94550_.getTabListDisplayName() != null ? decorateName(p_94550_, p_94550_.getTabListDisplayName().copy()) : decorateName(p_94550_, PlayerTeam.formatNameForTeam(p_94550_.getTeam(), Component.literal(p_94550_.getProfile().getName())));
    }

    private static Component decorateName(PlayerInfo p_94552_, MutableComponent p_94553_) {
        return p_94552_.getGameMode() == GameType.SPECTATOR ? p_94553_.withStyle(ChatFormatting.ITALIC) : p_94553_;
    }
    public static float getBombTime(float partialTicks) {
        return bombPlantedTickCount + partialTicks;
    }
    public static void setBombData(boolean bombExist, byte bombPos, int bombCountdownTicks) {
        ClientFpsData.bombExist = bombExist;
        ClientFpsData.bombPosition = bombPos <= 0 ? "" : bombPos == 1 ? "A点" : "B点";
        ClientFpsData.bombCountdownTicks = Math.max(0, bombCountdownTicks);
        bombPlantedTickCount = Math.max(0, BOMB_COUNTDOWN_TOTAL_TICKS - ClientFpsData.bombCountdownTicks);
        if (bombExist && ClientFpsData.bombCountdownTicks > 0) {
            requestBombCountdownRender(ClientFpsData.bombCountdownTicks);
        } else {
            bombCountdownRenderTicks = 0;
            bombCountdownRenderTimer = 0;
        }
    }
    public static void tick() {
        if (bombCountdownRenderTimer > 0) {
            bombCountdownRenderTimer--;
        }
        if (!bombExist || bombCountdownTicks <= 0) {
            return;
        }
        if (shouldPlayBombBeep(bombCountdownTicks)) {
            playBombBeep();
        }
        bombCountdownTicks--;
        bombPlantedTickCount++;
        if (bombCountdownTicks <= 0) {
            bombCountdownTicks = 0;
            onBombCountdownFinished();
        }
        if (bombCountdownTicks > 0 && bombPlantedTickCount % BOMB_COUNTDOWN_REQUEST_INTERVAL == 0) {
            requestBombCountdownRender(bombCountdownTicks);
        }
    }
    public static boolean shouldRenderBombCountdown() {
        return bombExist && bombCountdownRenderTimer > 0;
    }
    public static void requestBombCountdownRender(int countdownTicks) {
        bombCountdownRenderTicks = Math.max(0, countdownTicks);
        bombCountdownRenderTimer = BOMB_COUNTDOWN_PROMPT_DURATION;
    }
    public static int getDisplayBombSeconds(int countdownTicks) {
        return Math.max(0, (countdownTicks + 19) / 20);
    }
    public static void onBombCountdownFinished() {
    }
    private static boolean shouldPlayBombBeep(int remainingTicks) {
        return remainingTicks > 0 && remainingTicks % getBombBeepIntervalTicks(remainingTicks) == 0;
    }
    private static int getBombBeepIntervalTicks(int remainingTicks) {
        if (remainingTicks > 25 * 20) {
            return 20;
        }
        if (remainingTicks > 10 * 20) {
            return 10;
        }
        return 5;
    }
    private static void playBombBeep() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null) {
            return;
        }
        Vec3 bombPos = getBombSoundPos(player);
        if (bombPos == null) {
            return;
        }
        ClientProxy.playSoundNoDelayed(bombPos.x, bombPos.y, bombPos.z, SoundsInit.C4_BEEP2.get(), SoundSource.PLAYERS, 2F, 1.0F, true, player.level().random.nextLong());
    }
    private static Vec3 getBombSoundPos(LocalPlayer player) {
        List<C4Entity> c4Entities = player.level().getEntitiesOfClass(C4Entity.class, new AABB(player.blockPosition()).inflate(64.0D));
        C4Entity nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (C4Entity c4Entity : c4Entities) {
            double distance = c4Entity.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = c4Entity;
            }
        }
        return nearest == null ? null : nearest.position();
    }
}
