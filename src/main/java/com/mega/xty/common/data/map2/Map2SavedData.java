package com.mega.xty.common.data.map2;

import com.mega.endinglib.api.data.CompoundTagReader;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.api.data.CompoundTagWriter;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.*;
import com.mega.xty.util.data_expand.SavedDataGetter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Map2SavedData extends SavedData {
    public MinecraftServer server;
    private int playerCountNeed;
    private int countdown;
    private int redScore;
    private int blueScore;
    private boolean teamScoreVisible = true;
    private boolean isTeamMode = false;
    private boolean scoreOverlayVisible = false;
    private boolean isStopped = true;
    @Nullable
    private Component rightTopText;
    @Nullable
    private BlockPos pointA;
    @Nullable
    private BlockPos pointB;
    private int redWins;
    private int blueWins;
    private final Map<UUID, Inventory> deadSavedInventory = new Object2ObjectOpenHashMap<>();
    private Map2Functions map2Functions = new Map2Functions(this);
    public static Map2SavedData readOrCreate(MinecraftServer server) {
        Map2SavedData data = server.overworld().getDataStorage().computeIfAbsent(tag-> load(tag,server), Map2SavedData::new, "xty_map2");
        data.server = server;
        return data;
    }
    public static Map2SavedData getInstance(MinecraftServer server) {
        return ((SavedDataGetter) ((ServerEC) server).endinglib$serverECData()).getMap2SavedData();
    }
    public static Map2SavedData load(CompoundTag tag, MinecraftServer server) {
        Map2SavedData data = new Map2SavedData();
        if (CompoundTagUtils.containsBoolean(tag, "Stopped"))
            data.isStopped = tag.getBoolean("Stopped");
        data.isTeamMode = tag.getBoolean("isTeamMode");
        data.scoreOverlayVisible = tag.getBoolean("scoreOverlayVisible");
        if (CompoundTagUtils.containsInt(tag, "playerCountNeed"))
            data.playerCountNeed = tag.getInt("playerCountNeed");
        if (CompoundTagUtils.containsCompound(tag, "Map2Functions"))
            data.map2Functions = Map2Functions.load(tag.getCompound("Map2Functions"), server, data);
        if (CompoundTagUtils.containsInt(tag, "countdown"))
            data.countdown = tag.getInt("countdown");
        if (CompoundTagUtils.containsInt(tag, "redScore"))
            data.redScore = tag.getInt("redScore");
        if (CompoundTagUtils.containsInt(tag, "blueScore"))
            data.redScore = tag.getInt("blueScore");
        if (CompoundTagUtils.containsInt(tag, "redWins"))
            data.redWins = tag.getInt("redWins");
        if (CompoundTagUtils.containsInt(tag, "blueWins"))
            data.blueWins = tag.getInt("blueWins");
        data.teamScoreVisible = tag.getBoolean("teamScoreVisible");
        data.isTeamMode = tag.getBoolean("isTeamMode");
        if (CompoundTagUtils.containsIntArray(tag, "pointA"))  {
            var i = tag.getIntArray("pointA");
            if (i.length >= 3)
                data.pointA = new BlockPos(i[0], i[1], i[2]);
            else data.pointA = null;
        } else data.pointA = null;
        if (CompoundTagUtils.containsIntArray(tag, "pointB"))  {
            var i = tag.getIntArray("pointB");
            if (i.length >= 3)
                data.pointB = new BlockPos(i[0], i[1], i[2]);
            else data.pointB = null;
        } else data.pointB = null;
        if (CompoundTagUtils.containsString(tag, "RightTopText"))
            data.rightTopText = Component.Serializer.fromJson(tag.getString("RightTopText"));
        if (CompoundTagUtils.containsMap(tag, "DeadSavedInventories")) {
            data.deadSavedInventory.clear();
            data.deadSavedInventory.putAll(CompoundTagUtils.getMap(tag, "DeadSavedInventories", CompoundTag::getUUID, Inventory.NBT_READER));
        }
        return data;
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putBoolean("Stopped", this.isStopped);
        tag.putBoolean("isTeamMode", this.isTeamMode);
        tag.putInt("playerCountNeed", this.playerCountNeed);
        CompoundTag functions = new CompoundTag();
        if (this.map2Functions != null) this.map2Functions.save(functions);
        tag.put("Map2Functions", functions);
        tag.putInt("countdown", this.countdown);
        tag.putInt("redScore", this.redScore);
        tag.putInt("blueScore", this.blueScore);
        tag.putInt("redWins", this.redWins);
        tag.putInt("blueWins", this.blueWins);
        tag.putBoolean("teamScoreVisible", this.teamScoreVisible);
        tag.putBoolean("scoreOverlayVisible", this.scoreOverlayVisible);
        if (this.pointA != null)
            tag.putIntArray("pointA", new int[] {this.pointA.getX(), this.pointA.getY(), this.pointA.getZ()});
        if (this.pointB != null)
            tag.putIntArray("pointB", new int[] {this.pointB.getX(), this.pointB.getY(), this.pointB.getZ()});
        if (this.rightTopText != null)
            tag.putString("RightTopText", Component.Serializer.toJson(this.rightTopText));
        if (!this.deadSavedInventory.isEmpty())
            CompoundTagUtils.putMap(tag, "DeadSavedInventories", this.deadSavedInventory, CompoundTag::putUUID, Inventory.NBT_WRITER);
        return tag;
    }

    public Map2Functions getMap2Functions() {
        return map2Functions;
    }
    public void setPointA(BlockPos pos) {
        if (!Objects.equals(pointA, pos)) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CSyncPointsPacket(pos, this.pointB), serverPlayer);
        }
        this.pointA = pos;
    }

    public @Nullable BlockPos getPointA() {
        return pointA;
    }
    public void setPointB(BlockPos pos) {
        if (!Objects.equals(pointB, pos)) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CSyncPointsPacket(this.pointA, pos), serverPlayer);
        }
        this.pointB = pos;
    }

    public @Nullable BlockPos getPointB() {
        return pointB;
    }
    public void setStopped(boolean stopped) {
        if (this.isStopped != stopped) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CMap2StatsPacket(stopped), serverPlayer);
        }
        isStopped = stopped;
    }
    public boolean isStopped() {
        return isStopped;
    }
    public void setMode(boolean team) {
        if (this.isTeamMode != team) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CMap2ModePacket(team), serverPlayer);
        }
        isTeamMode = team;
    }
    public boolean isTeamMode() {
        return isTeamMode;
    }
    public void setScoreOverlayVisible(boolean visible) {
        if (this.scoreOverlayVisible != visible) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CMap2ScoreOverlayVisiblePacket(visible), serverPlayer);
        }
        scoreOverlayVisible = visible;
    }
    public boolean isScoreOverlayVisible() {
        return scoreOverlayVisible;
    }
    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
        this.setDirty();
    }
    public void setTeamScoreVisible(boolean visible) {
        if (this.teamScoreVisible != visible) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CMap2TeamScoreVisiblePacket(visible), serverPlayer);
        }
        teamScoreVisible = visible;
    }
    public boolean isTeamScoreVisible() {
        return teamScoreVisible;
    }
    public int getRedScore() {
        return redScore;
    }

    public void setRedScore(int redScore) {
        this.redScore = redScore;
        this.setDirty();
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
            NetworkHandler.sendToPlayer(new S2CSyncTeamScorePacket(redScore, this.blueScore), serverPlayer);
    }

    public int getBlueScore() {
        return blueScore;
    }

    public void setBlueScore(int blueScore) {
        this.blueScore = blueScore;
        this.setDirty();
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
            NetworkHandler.sendToPlayer(new S2CSyncTeamScorePacket(this.redScore, blueScore), serverPlayer);
    }
    public int getRedWins() {
        return redWins;
    }

    public void setRedWins(int redWins) {
        this.redWins = redWins;
        this.setDirty();
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
            NetworkHandler.sendToPlayer(new S2CSyncTeamWinsPacket(redWins, this.blueWins), serverPlayer);
    }

    public int getBlueWins() {
        return blueWins;
    }

    public void setBlueWins(int blueWins) {
        this.blueWins = blueWins;
        this.setDirty();
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
            NetworkHandler.sendToPlayer(new S2CSyncTeamWinsPacket(this.redWins, blueWins), serverPlayer);
    }
    public int getPlayerCountNeed() {
        return playerCountNeed;
    }
    public void setPlayerCountNeed(int countNeed) {
        this.playerCountNeed = countNeed;
        this.setDirty();
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
            NetworkHandler.sendToPlayer(new S2CPlayerCountNeedPacket(this.playerCountNeed), serverPlayer);
    }

    @Nullable
    public Component getRightTopText() {
        return rightTopText;
    }
    public void setRightTopText(@Nullable Component rightTopText) {
        if (!Objects.equals(rightTopText, this.rightTopText)) {
            this.rightTopText = rightTopText;
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CMap2TextTipPacket(rightTopText != null, rightTopText), serverPlayer);
        }
    }

    public Map<UUID, Inventory> getDeadSavedInventory() {
        return Collections.unmodifiableMap(deadSavedInventory);
    }
    public void storeDeadPlayerInventory(Player player) {
        deadSavedInventory.put(player.getUUID(), Inventory.createFromInventory(player.getInventory()));
        this.setDirty();
    }
    @Nullable
    public Inventory pickUpDeadSavedInv(Player player) {
        return deadSavedInventory.remove(player.getUUID());
    }

    public static class Inventory {
        public static final CompoundTagWriter<Inventory> NBT_WRITER = ((compoundTag, s, inventory) -> compoundTag.put(s, inventory.saveInventory(new ListTag())));
        public static final CompoundTagReader<Inventory> NBT_READER = ((compoundTag, s) -> {
            Inventory inventory = new Inventory();
            inventory.loadInventory(compoundTag.getList(s, Tag.TAG_COMPOUND));
            return inventory;
        });
        public final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
        public final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
        public final NonNullList<ItemStack> offhand = NonNullList.withSize(1, ItemStack.EMPTY);
        public ListTag saveInventory(ListTag listTag) {
            for(int i = 0; i < this.items.size(); ++i) {
                if (!this.items.get(i).isEmpty()) {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putByte("Slot", (byte)i);
                    this.items.get(i).save(compoundtag);
                    listTag.add(compoundtag);
                }
            }

            for(int j = 0; j < this.armor.size(); ++j) {
                if (!this.armor.get(j).isEmpty()) {
                    CompoundTag compoundtag1 = new CompoundTag();
                    compoundtag1.putByte("Slot", (byte)(j + 100));
                    this.armor.get(j).save(compoundtag1);
                    listTag.add(compoundtag1);
                }
            }

            for(int k = 0; k < this.offhand.size(); ++k) {
                if (!this.offhand.get(k).isEmpty()) {
                    CompoundTag compoundtag2 = new CompoundTag();
                    compoundtag2.putByte("Slot", (byte)(k + 150));
                    this.offhand.get(k).save(compoundtag2);
                    listTag.add(compoundtag2);
                }
            }

            return listTag;
        }

        public void loadInventory(ListTag listTag) {
            this.items.clear();
            this.armor.clear();
            this.offhand.clear();

            for(int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundtag = listTag.getCompound(i);
                int j = compoundtag.getByte("Slot") & 255;
                ItemStack itemstack = ItemStack.of(compoundtag);
                if (!itemstack.isEmpty()) {
                    if (j >= 0 && j < this.items.size()) {
                        this.items.set(j, itemstack);
                    } else if (j >= 100 && j < this.armor.size() + 100) {
                        this.armor.set(j - 100, itemstack);
                    } else if (j >= 150 && j < this.offhand.size() + 150) {
                        this.offhand.set(j - 150, itemstack);
                    }
                }
            }

        }
        public static Inventory createFromInventory(net.minecraft.world.entity.player.Inventory pInventory) {
            Inventory inventory = new Inventory();
            for (int i=0;i<inventory.items.size();i++)
                inventory.items.set(i, pInventory.items.get(i));
            for (int i=0;i<inventory.armor.size();i++)
                inventory.armor.set(i, pInventory.armor.get(i));
            for (int i=0;i<inventory.offhand.size();i++)
                inventory.offhand.set(i, pInventory.offhand.get(i));
            return inventory;
        }
        public void overridePlayerInv(Player player) {
            net.minecraft.world.entity.player.Inventory pInv = player.getInventory();
            for (int i=0;i<this.items.size();i++)
                pInv.items.set(i, this.items.get(i));
            for (int i=0;i<this.armor.size();i++)
                pInv.armor.set(i, this.armor.get(i));
            for (int i=0;i<this.offhand.size();i++)
                pInv.offhand.set(i, this.offhand.get(i));
        }
    }
}
