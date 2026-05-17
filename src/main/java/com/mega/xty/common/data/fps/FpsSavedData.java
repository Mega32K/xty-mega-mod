package com.mega.xty.common.data.fps;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.xty.common.data.fps.kad.ServerSynchedKADData;
import com.mega.xty.common.data.fps.kad.SynchedKADData;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.network.s2c.fps.S2CWeaponWarehouseBlacklistPacket;
import com.mega.xty.common.network.s2c.warehouse.S2CSyncWeaponWarehousePacket;
import com.mega.xty.common.entity.C4Entity;
import com.mega.xty.common.init.SoundsInit;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.fps.S2CBombDataPacket;
import com.mega.xty.common.network.s2c.fps.S2CRoundLoseRenderPacket;
import com.mega.xty.common.network.s2c.fps.S2CRoundWinRenderPacket;
import com.mega.xty.common.network.s2c.fps.S2CUsingKADPacket;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.util.data_expand.SavedDataGetter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.commands.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class FpsSavedData extends SavedData {
    public static final int BOMB_COUNTDOWN_TOTAL_TICKS = 40 * 20;
    public long tickCount = 0;
    private boolean enableKAD = false;
    private boolean kadDirty = false;
    private boolean bombExist = false;
    private int bombCountdownTicks = 0;
    //
    private byte bombPosition = 0;
    private Map<UUID, ServerSynchedKADData> kadData;
    private boolean playerNamesDirty = false;
    private Map<UUID, TabData> playerTabData;
    private final Set<UUID> removedPlayerTabData = new ObjectOpenHashSet<>();
    private Set<ResourceLocation> warehouseGunBlacklist = new LinkedHashSet<>();


    public MinecraftServer server;
    public FpsSavedData() {
    }
    public static FpsSavedData readOrCreate(MinecraftServer server) {
        FpsSavedData data = server.overworld().getDataStorage().computeIfAbsent(tag-> load(tag, server), () -> {
            FpsSavedData sd = new FpsSavedData();
            sd.setKadData(new Object2ObjectOpenHashMap<>());
            sd.setPlayerTabData(new Object2ObjectOpenHashMap<>());
            sd.warehouseGunBlacklist = new LinkedHashSet<>();
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
        if (tag.contains("WarehouseGunBlacklist", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("WarehouseGunBlacklist", Tag.TAG_STRING);
            Set<ResourceLocation> blacklist = new LinkedHashSet<>();
            for (int i = 0; i < listTag.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(listTag.getString(i));
                if (id != null) {
                    blacklist.add(id);
                }
            }
            data.warehouseGunBlacklist = blacklist;
        }
        data.enableKAD = tag.getBoolean("enableKAD");
        data.bombExist = tag.getBoolean("bombExist");
        data.bombPosition = tag.getByte("bombPosition");
        data.bombCountdownTicks = tag.getInt("bombCountdownTicks");
        return data;
    }
    public boolean isBombExist() {
        return bombExist;
    }

    public void setBombExist(boolean bombExist) {
        if (this.bombExist != bombExist) {
            this.setDirty();
        }
        this.bombExist = bombExist;
    }
    public byte getBombPosition() {
        return bombPosition;
    }
    public void setBombPosition(byte bombPosition) {
        if (this.bombPosition != bombPosition) {
            this.setDirty();
        }
        this.bombPosition = bombPosition;
    }
    public int getBombCountdownTicks() {
        return bombCountdownTicks;
    }
    public void setBombCountdownTicks(int bombCountdownTicks) {
        int value = Math.max(0, bombCountdownTicks);
        if (this.bombCountdownTicks != value) {
            this.setDirty();
        }
        this.bombCountdownTicks = value;
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
            com.mega.xty.common.network.NetworkHandler.sendToPlayer(new S2CBombDataPacket(this.bombExist, this.bombPosition, this.bombCountdownTicks), serverPlayer);
    }
    public void tickBombCountdown() {
        if (bombExist && bombCountdownTicks > 0) {
            bombCountdownTicks--;
            this.setDirty();
            if (bombCountdownTicks <= 0) {
                bombCountdownTicks = 0;
                explodeBombEffects();
                this.bombExist = false;
                this.bombPosition = 0;
                this.setBombCountdownTicks(0);
                onBombCountdownFinished();
            }
        }
    }
    public void onBombCountdownFinished() {
        Map2SavedData.getInstance(server).finish(true);
    }
    private void explodeBombEffects() {
        Map2SavedData map2SavedData = Map2SavedData.getInstance(server);
        Set<AABB> queryBoxes = new ObjectOpenHashSet<>();
        if (map2SavedData.getPointA() != null) {
            queryBoxes.add(new AABB(map2SavedData.getPointA()).inflate(32.0D));
        }
        if (map2SavedData.getPointB() != null) {
            queryBoxes.add(new AABB(map2SavedData.getPointB()).inflate(32.0D));
        }
        String func = map2SavedData.getMap2Functions().getCountdownStopFunction();
        for (ServerLevel level : server.getAllLevels()) {
            for (AABB queryBox : queryBoxes) {
                if (!Game2SavedData.getInstance(server).isStopped()) {
                    for (Player alivePlayer : level.getEntitiesOfClass(Player.class, queryBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                        CommonProxy.getMap2Cap(alivePlayer).ifPresent(cap -> {
                            if (!cap.isXaeroDead()) {
                                cap.setXaeroDead(true);
                                if (func != null && !func.isEmpty())
                                    server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f -> server.getFunctions().execute(f, alivePlayer.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2)));
                            }
                        });
                    }
                }
                for (C4Entity c4Entity : level.getEntitiesOfClass(C4Entity.class, queryBox)) {
                    Vec3 pos = c4Entity.position().add(0.0D, 0.2D, 0.0D);
                    level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 4, 0.0D, 0.0D, 0.0D, 0.0D);
                    level.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 16, 0.35D, 0.12D, 0.35D, 0.02D);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 24, 0.45D, 0.18D, 0.45D, 0.02D);
                    level.playSound(null, pos.x, pos.y, pos.z, SoundsInit.C4_EXPLODE1.get(), SoundSource.PLAYERS, 2.6F, 1.0F);
                    level.playSound(null, pos.x, pos.y, pos.z, level.random.nextBoolean() ? SoundsInit.C4_EXP_DEB1.get() : SoundsInit.C4_EXP_DEB2.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
                    c4Entity.remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED);
                }
            }
        }
    }
    public void setPlayerTabData(Map<UUID, TabData> playerTabData) {
        this.playerTabData = playerTabData instanceof Object2ObjectOpenHashMap<UUID, TabData> map ? map : new Object2ObjectOpenHashMap<>(playerTabData);
        this.removedPlayerTabData.clear();
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
        if (!this.warehouseGunBlacklist.isEmpty()) {
            ListTag listTag = new ListTag();
            for (ResourceLocation id : this.warehouseGunBlacklist) {
                listTag.add(net.minecraft.nbt.StringTag.valueOf(id.toString()));
            }
            compoundTag.put("WarehouseGunBlacklist", listTag);
        }
        compoundTag.putBoolean("enableKAD", this.enableKAD);
        compoundTag.putBoolean("bombExist", this.bombExist);
        compoundTag.putByte("bombPosition", this.bombPosition);
        compoundTag.putInt("bombCountdownTicks", this.bombCountdownTicks);
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
        for (var entry : kadData.entrySet()) {
            if (entry.getValue().isDirty()) {
                map.put(entry.getKey(), entry.getValue());
                entry.getValue().setDirty(false);
            }
        }
        this.setKadDirty(false);
        return map;
    }
    public TabData getOrPutPlayerTab(Player player) {
        return updatePlayerTab(player);
    }
    public void setPlayerTab(Player player) {
        updatePlayerTab(player);
    }
    private TabData updatePlayerTab(Player player) {
        TabData nameData = this.playerTabData.get(player.getUUID());
        MutableBoolean dead = new MutableBoolean(false);
        CommonProxy.getMap2Cap(player).ifPresent(cap -> dead.setValue(cap.isXaeroDead()));
        String teamName = getTeamName(player);
        Component displayName = getPlayerTabDisplayName(player);
        if (nameData == null || !Objects.equals(nameData.component, displayName) || nameData.isDead != dead.getValue()) {
            nameData = new TabData(dead.getValue(), displayName);
            nameData.teamName = teamName;
            this.playerTabData.put(player.getUUID(), nameData);
            this.removedPlayerTabData.remove(player.getUUID());
            nameData.setDirty(true);
            this.setPlayerNamesDirty(true);
            this.setDirty();
            return nameData;
        }
        if (!Objects.equals(nameData.teamName, teamName)) {
            nameData.setTeamName(teamName);
            this.setPlayerNamesDirty(true);
            this.setDirty();
        }
        return nameData;
    }
    private Component getPlayerTabDisplayName(Player player) {
        return com.mega.endinglib.proxy.CommonProxy.getCameraCapOptional(player)
                .map(cap -> cap.getDisplayNameOpt()
                        .<Component>map(displayName -> PlayerTeam.formatNameForTeam(player.getTeam(), displayName.copy()))
                        .orElseGet(player::getDisplayName))
                .orElseGet(player::getDisplayName);
    }
    public void removePlayerTab(Player player) {
        if (this.playerTabData.remove(player.getUUID()) != null) {
            this.removedPlayerTabData.add(player.getUUID());
            this.setPlayerNamesDirty(true);
            this.setDirty();
        }
    }
    public Map<UUID, TabData> getPlayerTabData() {
        return Collections.unmodifiableMap(playerTabData);
    }
    public Map<UUID, TabData> packDirtyTabs() {
        Map<UUID, TabData> map = new Object2ObjectOpenHashMap<>();
        for (var entry : playerTabData.entrySet()) {
            if (entry.getValue().isDirty()) {
                map.put(entry.getKey(), entry.getValue());
                entry.getValue().setDirty(false);
            }
        }
        this.setPlayerNamesDirty(false);
        return map;
    }
    public Set<UUID> packRemovedPlayerTabs() {
        Set<UUID> set = new ObjectOpenHashSet<>(this.removedPlayerTabData);
        this.removedPlayerTabData.clear();
        return set;
    }

    public Set<ResourceLocation> getWarehouseGunBlacklist() {
        return Collections.unmodifiableSet(this.warehouseGunBlacklist);
    }

    public boolean isWarehouseGunBlacklisted(ResourceLocation id) {
        return id != null && this.warehouseGunBlacklist.contains(id);
    }

    public void setWarehouseGunBlacklist(Collection<ResourceLocation> blacklist) {
        Set<ResourceLocation> cleaned = new LinkedHashSet<>();
        for (ResourceLocation id : blacklist) {
            if (id != null) {
                cleaned.add(id);
            }
        }
        if (!this.warehouseGunBlacklist.equals(cleaned)) {
            this.warehouseGunBlacklist = cleaned;
            this.setDirty();
            syncWarehouseGunBlacklist();
            sanitizeWeaponWarehouses();
        }
    }

    public boolean addWarehouseGunBlacklist(ResourceLocation id) {
        if (id == null || this.warehouseGunBlacklist.contains(id)) {
            return false;
        }
        Set<ResourceLocation> updated = new LinkedHashSet<>(this.warehouseGunBlacklist);
        updated.add(id);
        setWarehouseGunBlacklist(updated);
        return true;
    }

    public boolean removeWarehouseGunBlacklist(ResourceLocation id) {
        if (id == null || !this.warehouseGunBlacklist.contains(id)) {
            return false;
        }
        Set<ResourceLocation> updated = new LinkedHashSet<>(this.warehouseGunBlacklist);
        updated.remove(id);
        setWarehouseGunBlacklist(updated);
        return true;
    }

    public void clearWarehouseGunBlacklist() {
        if (!this.warehouseGunBlacklist.isEmpty()) {
            setWarehouseGunBlacklist(Collections.emptySet());
        }
    }

    private void syncWarehouseGunBlacklist() {
        if (this.server == null) {
            return;
        }
        for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(new S2CWeaponWarehouseBlacklistPacket(this.warehouseGunBlacklist), serverPlayer);
        }
    }

    private void sanitizeWeaponWarehouses() {
        if (this.server == null) {
            return;
        }
        for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
            CommonProxy.getWeaponWarehouseCap(serverPlayer).ifPresent(cap -> {
                var sanitized = com.mega.xty.common.warehouse.WeaponWarehouseItems.sanitizeSnapshot(cap.getWeaponWarehouse(), this.warehouseGunBlacklist);
                cap.setWeaponWarehouse(sanitized);
                NetworkHandler.sendToPlayer(new S2CSyncWeaponWarehousePacket(cap.getWeaponWarehouse()), serverPlayer);
            });
        }
    }

    private static String getTeamName(Player player) {
        return player.getTeam() == null ? null : player.getTeam().getName();
    }
    public void tick() {
        tickCount++;
        if (tickCount % 10 == 0) {
            PlayerList playerList = server.getPlayerList();
            for (var entry : playerTabData.entrySet()) {
                ServerPlayer player = playerList.getPlayer(entry.getKey());
                if (player != null) {
                    String currentTeam = getTeamName(player);
                    if (!Objects.equals(currentTeam, entry.getValue().teamName)) {
                        updatePlayerTab(player);
                    }

                }
            }
        }
    }
}
