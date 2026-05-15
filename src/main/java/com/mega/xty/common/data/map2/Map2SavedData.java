package com.mega.xty.common.data.map2;

import com.mega.endinglib.api.data.CompoundTagReader;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.api.data.CompoundTagWriter;
import com.mega.endinglib.api.item.component.ComponentChanges;
import com.mega.endinglib.api.item.component.DataComponents;
import com.mega.endinglib.api.item.component.ItemComponentManager;
import com.mega.endinglib.api.server.LambdaServerTask;
import com.mega.endinglib.common.data.EndingLibrarySavedData;
import com.mega.endinglib.common.data.InputOperations;
import com.mega.xty.common.capability.Map2Capability;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.entity.C4Entity;
import com.mega.xty.common.network.s2c.fps.S2CRoundStartRenderPacket;
import com.mega.xty.common.network.s2c.map2.game2.S2CGame2StartEffectPacket;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.endinglib.util.java.Args;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.fps.S2CRoundLoseRenderPacket;
import com.mega.xty.common.network.s2c.fps.S2CRoundWinRenderPacket;
import com.mega.xty.common.network.s2c.map2.*;
import com.mega.xty.common.init.ItemInit;
import com.mega.endinglib.common.network.PacketHandler;
import com.mega.xty.common.options.map2game2.Game2ServerOptions;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.util.data_expand.SavedDataGetter;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.util.AttachmentDataUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.xjqsh.lrtactical.entity.GrenadeEntity;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.Team;
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
    private int maxWins;
    private final Map<UUID, Inventory> deadSavedInventory = new Object2ObjectOpenHashMap<>();
    private final List<BlockPos> redHome = new ObjectArrayList<>();
    private final List<BlockPos> blueHome = new ObjectArrayList<>();
    @Nullable
    private ResourceKey<Level> game2Dimension;
    private Map2Functions map2Functions = new Map2Functions(this);
    private static final int HOME_MAX_SIZE = 10;
    private boolean game2NextRoundPending;
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
        if (CompoundTagUtils.containsInt(tag, "maxWins"))
            data.maxWins = tag.getInt("maxWins");
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
        if (tag.contains("RedHome", Tag.TAG_LIST)) {
            data.redHome.clear();
            ListTag listTag = tag.getList("RedHome", Tag.TAG_INT_ARRAY);
            for (Tag value : listTag) {
                if (value instanceof net.minecraft.nbt.IntArrayTag intArrayTag) {
                    int[] array = intArrayTag.getAsIntArray();
                    if (array.length >= 3) {
                        data.redHome.add(new BlockPos(array[0], array[1], array[2]));
                    }
                }
            }
        }
        if (tag.contains("BlueHome", Tag.TAG_LIST)) {
            data.blueHome.clear();
            ListTag listTag = tag.getList("BlueHome", Tag.TAG_INT_ARRAY);
            for (Tag value : listTag) {
                if (value instanceof net.minecraft.nbt.IntArrayTag intArrayTag) {
                    int[] array = intArrayTag.getAsIntArray();
                    if (array.length >= 3) {
                        data.blueHome.add(new BlockPos(array[0], array[1], array[2]));
                    }
                }
            }
        }
        if (CompoundTagUtils.containsString(tag, "Game2Dimension")) {
            data.game2Dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("Game2Dimension")));
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
        tag.putInt("maxWins", this.maxWins);
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
        if (!this.redHome.isEmpty()) {
            ListTag listTag = new ListTag();
            for (BlockPos pos : this.redHome) {
                listTag.add(new net.minecraft.nbt.IntArrayTag(new int[] {pos.getX(), pos.getY(), pos.getZ()}));
            }
            tag.put("RedHome", listTag);
        }
        if (!this.blueHome.isEmpty()) {
            ListTag listTag = new ListTag();
            for (BlockPos pos : this.blueHome) {
                listTag.add(new net.minecraft.nbt.IntArrayTag(new int[] {pos.getX(), pos.getY(), pos.getZ()}));
            }
            tag.put("BlueHome", listTag);
        }
        if (this.game2Dimension != null) {
            tag.putString("Game2Dimension", this.game2Dimension.location().toString());
        }
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
    public int getMaxWins() {
        if (this.server != null) {
            Game2SavedData game2SavedData = Game2SavedData.getInstance(this.server);
            if (game2SavedData.getServerOptions().getMatch().hasLoadedMaxWins()) {
                return game2SavedData.getServerOptions().getMatch().getMaxWins();
            }
        }
        return maxWins;
    }
    public void setMaxWins(int maxWins) {
        if (this.server != null) {
            Game2SavedData game2SavedData = Game2SavedData.getInstance(this.server);
            game2SavedData.getServerOptions().getMatch().setMaxWins(maxWins);
            game2SavedData.onServerOptionsUpdated();
            this.maxWins = game2SavedData.getServerOptions().getMatch().getMaxWins();
            this.setDirty();
            return;
        }
        if (this.maxWins != maxWins) {
            this.maxWins = maxWins;
            this.setDirty();
        }
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

    public void syncLegacyMaxWinsFromOptions(int maxWins) {
        if (this.maxWins != maxWins) {
            this.maxWins = maxWins;
            this.setDirty();
        }
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
    public List<BlockPos> getRedHome() {
        return Collections.unmodifiableList(this.redHome);
    }
    public void setRedHome(List<BlockPos> homes) {
        updateHomeList(this.redHome, homes);
    }
    public List<BlockPos> getBlueHome() {
        return Collections.unmodifiableList(this.blueHome);
    }
    public void setBlueHome(List<BlockPos> homes) {
        updateHomeList(this.blueHome, homes);
    }
    public @Nullable ResourceKey<Level> getGame2Dimension() {
        return this.game2Dimension;
    }
    public void setGame2Dimension(@Nullable ResourceKey<Level> game2Dimension) {
        if (!Objects.equals(this.game2Dimension, game2Dimension)) {
            this.game2Dimension = game2Dimension;
            this.setDirty();
        }
    }
    public void storeDeadPlayerInventory(Player player) {
        deadSavedInventory.put(player.getUUID(), Inventory.createFromInventory(player.getInventory()));
        this.setDirty();
    }
    @Nullable
    public Inventory pickUpDeadSavedInv(Player player) {
        if (deadSavedInventory.containsKey(player.getUUID()))
            this.setDirty();
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
    public void finish(boolean redWin) {
        boolean game2Playing = !this.isStopped() && !Game2SavedData.getInstance(this.server).isStopped();
        if (game2Playing) {
            if (this.game2NextRoundPending) {
                return;
            }
            this.game2NextRoundPending = true;
            clearGame2RoundWorldEntities();
        }
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer sp : players) {
            NetworkHandler.sendToPlayer(new S2CRoundWinSoundPacket(redWin), sp);
        }
        if (redWin) {
            this.setRedWins(this.getRedWins() + 1);
            for (ServerPlayer serverPlayer : players) {
                Team team = serverPlayer.getTeam();
                if (team != null) {
                    if (team.getColor() == ChatFormatting.RED) {
                        NetworkHandler.sendToPlayer(new S2CRoundWinRenderPacket(), serverPlayer);
                    } else if (team.getColor() == ChatFormatting.BLUE) {
                        NetworkHandler.sendToPlayer(new S2CRoundLoseRenderPacket(), serverPlayer);
                    }
                }
            }
        } else {
            this.setBlueWins(this.getBlueWins() + 1);
            for (ServerPlayer serverPlayer : players) {
                Team team = serverPlayer.getTeam();
                if (team != null) {
                    if (team.getColor() == ChatFormatting.RED) {
                        NetworkHandler.sendToPlayer(new S2CRoundLoseRenderPacket(), serverPlayer);
                    } else if (team.getColor() == ChatFormatting.BLUE) {
                        NetworkHandler.sendToPlayer(new S2CRoundWinRenderPacket(), serverPlayer);
                    }
                }
            }
        }
        if (game2Playing) {
            scheduleGame2NextRound();
        }
    }
    public void backToHome(List<ServerPlayer> players) {
        ServerLevel targetLevel = getGame2ServerLevel();
        if (targetLevel == null) {
            return;
        }
        teleportTeamToHomes(players, targetLevel, ChatFormatting.RED, this.redHome);
        teleportTeamToHomes(players, targetLevel, ChatFormatting.BLUE, this.blueHome);
    }
    public void startGame2NewRound() {
        this.game2NextRoundPending = false;
        Game2ServerOptions.Match matchOptions = Game2SavedData.getInstance(this.server).getServerOptions().getMatch();
        int maxWins = this.getMaxWins();
        if (maxWins > 0 && (this.redWins >= maxWins || this.blueWins >= maxWins)) {
            this.setRedWins(0);
            this.setBlueWins(0);
            Game2SavedData.getInstance(this.server).setStopped(true);
            return;
        }
        runStartNewRoundFunction();
        if (Game2SavedData.getInstance(this.server).getServerOptions().getLoadout().isClearDroppedItemsOnNewRound()) {
            clearWorldDroppedItems();
        }
        clearGame2RoundWorldEntities();
        List<ServerPlayer> players = this.server.getPlayerList().getPlayers();
        EndingLibrarySavedData elData = EndingLibrarySavedData.getInstance(this.server);
        for (ServerPlayer player : players) {
            resetPlayerForNewRound(player);
        }
        Set<UUID> teleportedPlayers = backToHomeInternal(players);
        clearNewRoundInventory(players, teleportedPlayers);
        prepareRoundC4(players, teleportedPlayers);
        prepareBlueRoundItems(players, teleportedPlayers);
        int roundStartLockTicks = matchOptions.getRoundStartLockTicks();
        long unlockGameTime = this.server.overworld().getGameTime() + roundStartLockTicks;
        for (ServerPlayer player : players) {
            if (teleportedPlayers.contains(player.getUUID())) {
                equipRoundArmor(player);
                elData.addDisabledPermission(player, InputOperations.MOVE_FORWARD);
                elData.addDisabledPermission(player, InputOperations.MOVE_BACKWARD);
                elData.addDisabledPermission(player, InputOperations.MOVE_LEFT);
                elData.addDisabledPermission(player, InputOperations.MOVE_RIGHT);
                elData.addDisabledPermission(player, InputOperations.JUMP);
                elData.addDisabledPermission(player, InputOperations.MOUSE_ATTACK);
                elData.addDisabledPermission(player, InputOperations.MOUSE_USE);
                CommonProxy.getMap2Cap(player).ifPresent(cap -> cap.setRoundKeyboardUnlockGameTime(unlockGameTime));
                NetworkHandler.sendToPlayer(new S2CGame2StartEffectPacket(roundStartLockTicks), player);
            } else {
                CommonProxy.getMap2Cap(player).ifPresent(Map2Capability::clearRoundKeyboardUnlockGameTime);
                NetworkHandler.sendToPlayer(new S2CGame2StartEffectPacket(0), player);
            }
        }
        for (ServerPlayer player : players) {
            NetworkHandler.sendToPlayer(new S2CRoundStartRenderPacket(), player);
        }
        scheduleRoundKeyboardUnlock(teleportedPlayers, unlockGameTime);
    }

    private void runStartNewRoundFunction() {
        String func = Game2SavedData.getInstance(this.server).getGame2Functions().getStartNewRoundFunction();
        if (func != null && !func.isEmpty()) {
            CommandSourceStack sourceStack = this.server.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2);
            this.server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f -> this.server.getFunctions().execute(f, sourceStack));
        }
    }

    private Set<UUID> backToHomeInternal(List<ServerPlayer> players) {
        ServerLevel targetLevel = getGame2ServerLevel();
        if (targetLevel == null) {
            return Collections.emptySet();
        }
        Set<UUID> teleported = new HashSet<>();
        teleportTeamToHomes(players, targetLevel, ChatFormatting.RED, this.redHome, teleported);
        teleportTeamToHomes(players, targetLevel, ChatFormatting.BLUE, this.blueHome, teleported);
        return teleported;
    }

    private void teleportTeamToHomes(List<ServerPlayer> players, ServerLevel targetLevel, ChatFormatting teamColor, List<BlockPos> homes) {
        teleportTeamToHomes(players, targetLevel, teamColor, homes, null);
    }

    private void teleportTeamToHomes(List<ServerPlayer> players, ServerLevel targetLevel, ChatFormatting teamColor, List<BlockPos> homes, @Nullable Set<UUID> teleported) {
        if (homes.isEmpty()) {
            return;
        }
        int index = 0;
        for (ServerPlayer player : players) {
            Team team = player.getTeam();
            if (team == null || team.getColor() != teamColor) {
                continue;
            }
            if (index >= homes.size()) {
                break;
            }
            BlockPos homePos = homes.get(index);
            CommonProxy.getMap2Cap(player).ifPresent(Map2Capability::clearDeathStateData);
            player.teleportTo(targetLevel, homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, player.getYRot(), player.getXRot());
            if (teleported != null) {
                teleported.add(player.getUUID());
            }
            index++;
        }
    }

    @Nullable
    private ServerLevel getGame2ServerLevel() {
        if (this.game2Dimension == null) {
            return null;
        }
        return this.server.getLevel(this.game2Dimension);
    }

    private void resetPlayerForNewRound(ServerPlayer player) {
        FpsSavedData.getInstance(player.server).getOrPutKAD(player).setKAD(KAD.KAD_CURRENT, KAD.deserialize(0));
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            cap.setXaeroDead(false);
            cap.clearDeathStateData();
        });
        player.removeAllEffects();
        player.clearFire();
        player.setRemainingFireTicks(0);
        int roundStartHealth = Game2SavedData.getInstance(player.server).getServerOptions().getMatch().getRoundStartHealth();
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(roundStartHealth);
        player.setHealth(roundStartHealth);
        player.getFoodData().setFoodLevel(20);
        CommonProxy.getXtyCap(player).ifPresent(cap -> {
            cap.setGame2MaxHealth(roundStartHealth);
            cap.setGame2Health(roundStartHealth);
        });
    }

    private void clearNewRoundInventory(List<ServerPlayer> players, Set<UUID> teleportedPlayers) {
        for (ServerPlayer player : players) {
            if (!teleportedPlayers.contains(player.getUUID())) {
                continue;
            }
            clearItemList(player.getInventory().items);
            clearItemList(player.getInventory().armor);
            clearItemList(player.getInventory().offhand);
            player.containerMenu.setCarried(ItemStack.EMPTY);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }

    private void clearItemList(NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    private void clearRoundKeyboardInputs(EndingLibrarySavedData data, ServerPlayer player) {
        data.removeDisabledPermission(player, InputOperations.MOVE_FORWARD);
        data.removeDisabledPermission(player, InputOperations.MOVE_BACKWARD);
        data.removeDisabledPermission(player, InputOperations.MOVE_LEFT);
        data.removeDisabledPermission(player, InputOperations.MOVE_RIGHT);
        data.removeDisabledPermission(player, InputOperations.JUMP);
        data.removeDisabledPermission(player, InputOperations.MOUSE_ATTACK);
        data.removeDisabledPermission(player, InputOperations.MOUSE_USE);
    }
    private void equipRoundArmor(ServerPlayer player) {
        Team team = player.getTeam();
        if (team == null) {
            return;
        }
        Game2ServerOptions.Loadout loadoutOptions = Game2SavedData.getInstance(player.server).getServerOptions().getLoadout();
        if (team.getColor() == ChatFormatting.RED) {
            if (loadoutOptions.isEquipRedNanosuit()) {
                player.setItemSlot(EquipmentSlot.CHEST, ItemInit.OPTICAL_NANOSUIT.get().getDefaultInstance());
            }
        } else if (team.getColor() == ChatFormatting.BLUE) {
            ItemStack chestplate = Items.CHAINMAIL_CHESTPLATE.getDefaultInstance();
            ItemComponentManager.get(chestplate).mergeChangedToNBTAndUpdate(ComponentChanges
                    .builder(chestplate.getItem())
                    .add(DataComponents.MAX_DAMAGE, loadoutOptions.getBlueArmorDurability())
                    .build());
            chestplate.setDamageValue(0);
            player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        }
    }

    private void updateHomeList(List<BlockPos> target, List<BlockPos> homes) {
        List<BlockPos> trimmed = new ObjectArrayList<>(Math.min(HOME_MAX_SIZE, homes.size()));
        for (int i = 0; i < homes.size() && i < HOME_MAX_SIZE; i++) {
            trimmed.add(homes.get(i));
        }
        if (!target.equals(trimmed)) {
            target.clear();
            target.addAll(trimmed);
            this.setDirty();
        }
    }

    private void prepareRoundC4(List<ServerPlayer> players, Set<UUID> teleportedPlayers) {
        clearRoundC4State();
        clearRoundC4(players);
        Game2ServerOptions.Loadout loadoutOptions = Game2SavedData.getInstance(this.server).getServerOptions().getLoadout();
        List<ServerPlayer> redPlayers = new ObjectArrayList<>();
        for (ServerPlayer player : players) {
            if (!teleportedPlayers.contains(player.getUUID())) {
                continue;
            }
            Team team = player.getTeam();
            if (team != null && team.getColor() == ChatFormatting.RED) {
                CommonProxy.getWeaponWarehouseCap(player).ifPresent(cap -> cap.applySelectedWarehouseMelee(player));
                redPlayers.add(player);
            }
        }
        if (loadoutOptions.isGiveRedBomb() && !redPlayers.isEmpty()) {
            ServerPlayer c4Player = redPlayers.get(this.server.overworld().random.nextInt(redPlayers.size()));
            ItemStack c4 = ItemInit.C4_BOMB.get().getDefaultInstance();
            if (!c4Player.getInventory().add(c4)) {
                c4Player.drop(c4, false);
            }
        }
    }

    private void clearRoundC4State() {
        FpsSavedData fpsSavedData = FpsSavedData.getInstance(this.server);
        fpsSavedData.setBombPosition((byte) 0);
        fpsSavedData.setBombExist(false);
        fpsSavedData.setBombCountdownTicks(0);
        clearWorldC4Entities();
    }

    public void clearGame2RoundWorldEntities() {
        clearRoundC4State();
        clearWorldGrenadeEntities();
    }

    private void clearWorldC4Entities() {
        for (ServerLevel level : this.server.getAllLevels()) {
            List<C4Entity> c4Entities = new ObjectArrayList<>();
            for (var entity : level.getAllEntities()) {
                if (entity instanceof C4Entity c4Entity) {
                    c4Entities.add(c4Entity);
                }
            }
            for (C4Entity c4Entity : c4Entities) {
                c4Entity.discard();
            }
        }
    }

    private void clearWorldGrenadeEntities() {
        for (ServerLevel level : this.server.getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (entity instanceof ThrowableItemEntity) {
                    entity.discard();
                }
            }
        }
    }

    private void clearWorldDroppedItems() {
        for (ServerLevel level : this.server.getAllLevels()) {
            List<ItemEntity> itemEntities = new ObjectArrayList<>();
            for (var entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity) {
                    itemEntities.add(itemEntity);
                }
            }
            for (ItemEntity itemEntity : itemEntities) {
                itemEntity.discard();
            }
        }
    }

    private void prepareBlueRoundItems(List<ServerPlayer> players, Set<UUID> teleportedPlayers) {
        Game2ServerOptions.Loadout loadoutOptions = Game2SavedData.getInstance(this.server).getServerOptions().getLoadout();
        for (ServerPlayer player : players) {
            if (!teleportedPlayers.contains(player.getUUID())) {
                continue;
            }
            Team team = player.getTeam();
            if (team == null || team.getColor() != ChatFormatting.BLUE) {
                continue;
            }
            FpsSavedData fpsSavedData = FpsSavedData.getInstance(this.server);
            CommonProxy.getWeaponWarehouseCap(player).ifPresent(cap -> cap.applySelectedWarehouseLoadout(player, fpsSavedData.getWarehouseGunBlacklist()));
            if (loadoutOptions.isGiveBlueDefuseKit()) {
                giveBdkIfMissing(player);
            }
            if (loadoutOptions.isGiveBlueCreativeAmmoBox()) {
                giveAllTypeCreativeAmmoBox(player);
            }
            if (loadoutOptions.isFillBlueGunAmmo()) {
                fillInventoryGuns(player);
            }
        }
    }

    private void giveBdkIfMissing(ServerPlayer player) {
        if (hasBdk(player.getInventory().items) || hasBdk(player.getInventory().offhand)) {
            return;
        }
        ItemStack bdk = ItemInit.BDK.get().getDefaultInstance();
        if (!player.getInventory().add(bdk)) {
            player.drop(bdk, false);
        }
    }

    private void giveAllTypeCreativeAmmoBox(ServerPlayer player) {
        ItemStack ammoBox = com.tacz.guns.init.ModItems.AMMO_BOX.get().getDefaultInstance();
        if (ammoBox.getItem() instanceof IAmmoBox taczAmmoBox) {
            taczAmmoBox.setCreative(ammoBox, true);
        }
        if (!player.getInventory().add(ammoBox)) {
            player.drop(ammoBox, false);
        }
    }

    private boolean hasBdk(NonNullList<ItemStack> items) {
        for (ItemStack stack : items) {
            if (stack.is(ItemInit.BDK.get())) {
                return true;
            }
        }
        return false;
    }

    private void fillInventoryGuns(ServerPlayer player) {
        fillGunsInItemList(player.getInventory().items);
        fillGunsInItemList(player.getInventory().offhand);
    }

    private void fillGunsInItemList(NonNullList<ItemStack> items) {
        for (ItemStack stack : items) {
            if (stack.getItem() instanceof IGun gun) {
                TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).ifPresent(index -> {
                    int ammoCount = AttachmentDataUtils.getAmmoCountWithAttachment(stack, index.getGunData());
                    gun.setCurrentAmmoCount(stack, ammoCount);
                });
            }
        }
    }

    private void clearRoundC4(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            clearC4FromItemList(player.getInventory().items);
            clearC4FromItemList(player.getInventory().armor);
            clearC4FromItemList(player.getInventory().offhand);
        }
        boolean changed = false;
        for (Inventory inventory : this.deadSavedInventory.values()) {
            changed |= clearC4FromItemList(inventory.items);
            changed |= clearC4FromItemList(inventory.armor);
            changed |= clearC4FromItemList(inventory.offhand);
        }
        if (changed) {
            this.setDirty();
        }
    }

    private boolean clearC4FromItemList(NonNullList<ItemStack> items) {
        boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).is(ItemInit.C4_BOMB.get())) {
                items.set(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        return changed;
    }

    private void scheduleRoundKeyboardUnlock(Set<UUID> teleportedPlayers, long unlockGameTime) {
        if (teleportedPlayers.isEmpty()) {
            return;
        }
        new LambdaServerTask(new Args(0), task -> {
            if (shouldUnlockRoundKeyboard(unlockGameTime)) {
                EndingLibrarySavedData elData = EndingLibrarySavedData.getInstance(this.server);
                for (UUID uuid : teleportedPlayers) {
                    ServerPlayer player = this.server.getPlayerList().getPlayer(uuid);
                    if (player != null) {
                        clearRoundKeyboardInputs(elData, player);
                        CommonProxy.getMap2Cap(player).ifPresent(Map2Capability::clearRoundKeyboardUnlockGameTime);
                    }
                }
            }
        }, task -> shouldUnlockRoundKeyboard(unlockGameTime)).addToManager();
    }

    private boolean shouldUnlockRoundKeyboard(long unlockGameTime) {
        return Game2SavedData.getInstance(this.server).isStopped() || this.server.overworld().getGameTime() >= unlockGameTime;
    }

    private void scheduleGame2NextRound() {
        int delayTicks = Game2SavedData.getInstance(this.server).getServerOptions().getMatch().getNextRoundDelayTicks();
        new LambdaServerTask(new Args(0), task -> {
            int tick = task.getArgs().get(0);
            tick++;
            task.getArgs().set(0, tick);
            if (tick >= delayTicks && this.game2NextRoundPending) {
                this.game2NextRoundPending = false;
                if (!this.isStopped() && !Game2SavedData.getInstance(this.server).isStopped()) {
                    this.startGame2NewRound();
                }
            }
        }, task -> ((int) task.getArgs().get(0)) >= delayTicks).addToManager();
    }
}
