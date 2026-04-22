package com.mega.xty.common.data.fps;

import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.data.fps.kad.SynchedKADData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class ClientFpsData {
    public static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.<PlayerInfo>comparingInt(pInfo -> getPlayerKAD(pInfo).getOrDefaultKAD(KAD.KAD_GENERAL).kills).thenComparing(pInfo -> getPlayerKAD(pInfo).getOrDefaultKAD(KAD.KAD_GENERAL).assists).thenComparing(pInfo -> getPlayerKAD(pInfo).getOrDefaultKAD(KAD.KAD_GENERAL).deaths);
    public static boolean enabled;
    public static final Map<UUID, SynchedKADData> kadData = new Object2ObjectOpenHashMap<>();
    public static final Map<UUID, TabData> playerDisplayNames = new Object2ObjectOpenHashMap<>();
    public static int bombPlantedTickCount;
    public static boolean bombExist;
    public static String bombPosition;
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
}
