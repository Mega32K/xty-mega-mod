package com.mega.xty.common.data.fps;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.xty.common.data.fps.kad.ServerSynchedKADData;
import com.mega.xty.common.data.fps.kad.SynchedKADData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.fps.S2CBombDataPacket;
import com.mega.xty.common.network.s2c.fps.S2CUsingKADPacket;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.util.data_expand.SavedDataGetter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FpsSavedData extends SavedData {
    private boolean enableKAD = false;
    private boolean kadDirty = false;
    private boolean bombExist = false;
    //
    private byte bombPosition = 0;
    private Map<UUID, ServerSynchedKADData> kadData;
    private boolean playerNamesDirty = false;
    private Map<UUID, TabData> playerTabData;

    public boolean isBombExist() {
        return bombExist;
    }

    public void setBombExist(boolean bombExist) {
        if (this.bombExist != bombExist) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CBombDataPacket(bombExist, this.bombPosition), serverPlayer);
        }
        this.bombExist = bombExist;
    }
    public byte getBombPosition() {
        return bombPosition;
    }
    public void setBombPosition(byte bombPosition) {
        if (this.bombPosition != bombPosition) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CBombDataPacket(this.bombExist, bombPosition), serverPlayer);
        }
        this.bombPosition = bombPosition;
    }

    public MinecraftServer server;
    public FpsSavedData() {
    }
    public static FpsSavedData readOrCreate(MinecraftServer server) {
        FpsSavedData data = server.overworld().getDataStorage().computeIfAbsent(tag-> load(tag, server), () -> {
            FpsSavedData sd = new FpsSavedData();
            sd.setKadData(new Object2ObjectOpenHashMap<>());
            sd.setPlayerTabData(new Object2ObjectOpenHashMap<>());
            return sd;
        }, "xty_fps_saved_data");
        data.server = server;
        return data;
    }
    public static FpsSavedData getInstance(MinecraftServer server) {
        return ((SavedDataGetter) ((ServerEC) server).endinglib$serverECData()).getFpsSavedData();
    }
    public static FpsSavedData load(CompoundTag tag, MinecraftServer server) {
        FpsSavedData data = new FpsSavedData();
        data.setPlayerTabData(CompoundTagUtils.getMap(tag, "PlayerTabData", CompoundTag::getUUID, TabData.NBT_READER));
        data.setKadData(CompoundTagUtils.getMap(tag, "KAD", CompoundTag::getUUID, ServerSynchedKADData.NBT_READER.apply(data)));
        data.enableKAD = tag.getBoolean("enableKAD");
        data.bombExist = tag.getBoolean("bombExist");
        data.bombPosition = tag.getByte("bombPosition");
        return data;
    }
    public void setPlayerTabData(Map<UUID, TabData> playerTabData) {
        this.playerTabData = playerTabData instanceof Object2ObjectOpenHashMap<UUID, TabData> map ? map : new Object2ObjectOpenHashMap<>(playerTabData);
    }
    public void setKadData(Map<UUID, ServerSynchedKADData> kadData) {
        this.kadData = kadData instanceof Object2ObjectOpenHashMap<UUID, ServerSynchedKADData> map ? map : new Object2ObjectOpenHashMap<>(kadData);
    }
    public void clearKAD() {
        if (!this.kadData.isEmpty()) {
            this.kadData.clear();
            this.setDirty();
            this.setKadDirty(true);
        }
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        CompoundTagUtils.putMap(compoundTag, "PlayerTabData", playerTabData, CompoundTag::putUUID, TabData.NBT_WRITER);
        CompoundTagUtils.putMap(compoundTag, "KAD", kadData, CompoundTag::putUUID, ServerSynchedKADData.NBT_WRITER);
        compoundTag.putBoolean("enableKAD", this.enableKAD);
        compoundTag.putBoolean("bombExist", this.bombExist);
        compoundTag.putByte("bombPosition", this.bombPosition);
        return compoundTag;
    }
    public boolean isEnableKAD() {
        return enableKAD;
    }
    public void setEnableKAD(boolean enableKAD) {
        if (this.enableKAD != enableKAD) {
            this.setDirty();
            this.setKadDirty(true);
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CUsingKADPacket(enableKAD), serverPlayer);
        }
        this.enableKAD = enableKAD;
    }

    public Map<UUID, SynchedKADData> getKadData() {
        return Collections.unmodifiableMap(kadData);
    }
    public ServerSynchedKADData getOrPutKAD(Player player) {
        ServerSynchedKADData kad = this.kadData.get(player.getUUID());
        if (kad == null) {
            kad = ServerSynchedKADData.createDefaultKAD(this);
            this.kadData.put(player.getUUID(), kad);
            this.setKadDirty(true);
            this.setDirty();
            return kad;
        }
        return kad;
    }
    public boolean isKadDirty() {
        return kadDirty;
    }
    public void setKadDirty(boolean kadDirty) {
        this.kadDirty = kadDirty;
    }

    public boolean isPlayerNamesDirty() {
        return playerNamesDirty;
    }

    public void setPlayerNamesDirty(boolean playerNamesDirty) {
        this.playerNamesDirty = playerNamesDirty;
    }

    public Map<UUID, SynchedKADData> packDirtyKAD() {
        Map<UUID, SynchedKADData> map = new Object2ObjectOpenHashMap<>();
        for (var entry : kadData.entrySet())
            if (entry.getValue().isDirty()) {
                map.put(entry.getKey(), entry.getValue());
            }
        this.setKadDirty(false);
        return map;
    }
    public TabData getOrPutPlayerTab(Player player) {
        TabData nameData = this.playerTabData.get(player.getUUID());
        MutableBoolean dead = new MutableBoolean(false);
        CommonProxy.getMap2Cap(player).ifPresent(cap -> dead.setValue(cap.isXaeroDead()));
        if (nameData == null || !Objects.equals(nameData.component, player.getDisplayName()) || nameData.isDead != dead.getValue()) {
            nameData = new TabData(dead.getValue(), player.getDisplayName());
            this.playerTabData.put(player.getUUID(), nameData);
            nameData.setDirty(true);
            this.setPlayerNamesDirty(true);
            this.setDirty();
            return nameData;
        }
        return nameData;
    }
    public void setPlayerTab(Player player) {
        TabData nameData = this.playerTabData.get(player.getUUID());
        MutableBoolean dead = new MutableBoolean(false);
        CommonProxy.getMap2Cap(player).ifPresent(cap -> dead.setValue(cap.isXaeroDead()));
        if (nameData == null || !Objects.equals(nameData.component, player.getDisplayName()) || nameData.isDead != dead.getValue()) {
            nameData = new TabData(dead.getValue(), player.getDisplayName());
            this.playerTabData.put(player.getUUID(), nameData);
            nameData.setDirty(true);
            this.setPlayerNamesDirty(true);
            this.setDirty();
        }
    }
    public Map<UUID, TabData> getPlayerTabData() {
        return Collections.unmodifiableMap(playerTabData);
    }
    public Map<UUID, TabData> packDirtyTabs() {
        Map<UUID, TabData> map = new Object2ObjectOpenHashMap<>();
        for (var entry : playerTabData.entrySet())
            if (entry.getValue().isDirty()) {
                map.put(entry.getKey(), entry.getValue());
            }
        this.setPlayerNamesDirty(false);
        return map;
    }
}
