package com.mega.map.common.data.map2;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.common.data.EndingLibrarySavedData;
import com.mega.endinglib.common.data.InputOperations;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.map.common.data.fps.FpsSavedData;
import com.mega.map.common.data.fps.kad.KAD;
import com.mega.map.common.init.ItemInit;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.s2c.map2.game2.S2CGame2StatsPacket;
import com.mega.map.common.network.s2c.map2.game2.S2CSyncGame2ServerOptionsPacket;
import com.mega.map.common.network.s2c.map2.game2.S2CSyncGame2WarehouseMeleePacket;
import com.mega.map.common.options.map2game2.Game2ServerOptions;
import com.mega.map.common.options.map2game2.Game2ServerOptionsCache;
import com.mega.map.common.voicechat.Game2VoicechatGroups;
import com.mega.map.proxy.CommonProxy;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.mega.map.util.data_expand.SavedDataGetter;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Game2SavedData extends SavedData {
    private static final String EXTRA_WAREHOUSE_MELEE_KEY = "ExtraWarehouseMelee";
    private static final String SERVER_OPTIONS_KEY = "ServerOptions";
    public MinecraftServer server;
    private boolean isStopped = true;
    private Game2Functions game2Functions = new Game2Functions(this);
    private List<ItemStack> extraWarehouseMeleeStacks = new ArrayList<>();
    private final Game2ServerOptions serverOptions = new Game2ServerOptions();
    public static Game2SavedData readOrCreate(MinecraftServer server) {
        Game2SavedData data = server.overworld().getDataStorage().computeIfAbsent(tag-> load(tag,server), Game2SavedData::new, "xty_map2_game_2");
        data.server = server;
        return data;
    }
    public static Game2SavedData getInstance(MinecraftServer server) {
        return ((SavedDataGetter) ((ServerEC) server).endinglib$serverECData()).getMap2game2SavedData();
    }
    public static Game2SavedData load(CompoundTag tag, MinecraftServer server) {
        Game2SavedData data = new Game2SavedData();
        if (CompoundTagUtils.containsBoolean(tag, "Stopped"))
            data.isStopped = tag.getBoolean("Stopped");
        if (CompoundTagUtils.containsCompound(tag, "Game2Functions"))
            data.game2Functions = Game2Functions.load(tag.getCompound("Game2Functions"), server, data);
        if (tag.contains(EXTRA_WAREHOUSE_MELEE_KEY, Tag.TAG_LIST)) {
            ListTag listTag = tag.getList(EXTRA_WAREHOUSE_MELEE_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                ItemStack stack = ItemStack.of(listTag.getCompound(i));
                if (isValidWarehouseMelee(stack)) {
                    data.extraWarehouseMeleeStacks.add(stack);
                }
            }
        }
        if (tag.contains(SERVER_OPTIONS_KEY, Tag.TAG_COMPOUND)) {
            data.serverOptions.load(tag.getCompound(SERVER_OPTIONS_KEY));
        }
        Game2ServerOptionsCache.updateFrom(data.serverOptions);
        return data;
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putBoolean("Stopped", this.isStopped);
        CompoundTag functions = new CompoundTag();
        if (this.game2Functions != null) this.game2Functions.save(functions);
        tag.put("Game2Functions", functions);
        if (!this.extraWarehouseMeleeStacks.isEmpty()) {
            ListTag listTag = new ListTag();
            for (ItemStack stack : this.extraWarehouseMeleeStacks) {
                listTag.add(stack.save(new CompoundTag()));
            }
            tag.put(EXTRA_WAREHOUSE_MELEE_KEY, listTag);
        }
        tag.put(SERVER_OPTIONS_KEY, this.serverOptions.save(new CompoundTag()));
        return tag;
    }

    public void setStopped(boolean stopped) {
        if (this.isStopped != stopped) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                NetworkHandler.sendToPlayer(new S2CGame2StatsPacket(stopped), serverPlayer);
                NetworkHandler.sendToPlayer(new S2CSyncGame2WarehouseMeleePacket(this.extraWarehouseMeleeStacks), serverPlayer);
                NetworkHandler.sendToPlayer(new S2CSyncGame2ServerOptionsPacket(this.serverOptions), serverPlayer);
            }
        }
        if (stopped) {
            FpsSavedData fpsSavedData = FpsSavedData.getInstance(server);
            EndingLibrarySavedData elData = EndingLibrarySavedData.getInstance(this.server);
            Map2SavedData map2SavedData = Map2SavedData.getInstance(this.server);
            Game2VoicechatGroups.clearAllPlayers(this.server);
            map2SavedData.clearGame2RoundWorldEntities();
            for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
                CommonProxy.getMap2Cap(serverPlayer).ifPresent(cap -> {
                    cap.setXaeroDead(false);
                    cap.clearDeathStateData();
                });
                clearRoundControlInputs(elData, serverPlayer);
                resetPlayerToDefaultMaxHealth(serverPlayer);
                clearRoundArmor(serverPlayer);
                clearRoundInventory(serverPlayer);
                fpsSavedData.getOrPutKAD(serverPlayer).setKAD(KAD.KAD_CURRENT, KAD.deserialize(0));
                fpsSavedData.getOrPutKAD(serverPlayer).setKAD(KAD.KAD_GENERAL, KAD.deserialize(0));
            }
            clearDeadSavedRoundInventories(map2SavedData);
            teleportPlayersToRespawnPoints(this.server.getPlayerList().getPlayers());
            for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
                if (serverPlayer != null) {
                    CommandSourceStack sourceStack2 = serverPlayer.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2);
                    String func = this.game2Functions.getStopFunction();
                    if (func != null && !func.isEmpty()) {
                        server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f-> server.getFunctions().execute(f, sourceStack2));
                    }
                    break;
                }
            }
        }
        isStopped = stopped;
    }
    public boolean isStopped() {
        return isStopped;
    }
    public Game2Functions getGame2Functions() {
        return game2Functions;
    }

    public Game2ServerOptions getServerOptions() {
        return this.serverOptions;
    }

    public void onServerOptionsUpdated() {
        Game2ServerOptionsCache.updateFrom(this.serverOptions);
        Map2SavedData.getInstance(this.server).syncLegacyMaxWinsFromOptions(this.serverOptions.getMatch().getMaxWins());
        this.setDirty();
        for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(new S2CSyncGame2ServerOptionsPacket(this.serverOptions), serverPlayer);
        }
    }

    public List<ItemStack> getExtraWarehouseMeleeStacks() {
        return this.extraWarehouseMeleeStacks.stream().map(ItemStack::copy).toList();
    }

    public void setExtraWarehouseMeleeStacks(List<ItemStack> meleeStacks) {
        List<ItemStack> cleaned = new ArrayList<>();
        for (ItemStack stack : meleeStacks) {
            if (isValidWarehouseMelee(stack)) {
                boolean exists = cleaned.stream().anyMatch(existing -> ItemStack.isSameItemSameTags(existing, stack));
                if (!exists) {
                    cleaned.add(stack.copy());
                }
            }
        }
        if (!sameItemStackList(this.extraWarehouseMeleeStacks, cleaned)) {
            this.extraWarehouseMeleeStacks = cleaned;
            this.setDirty();
            for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
                NetworkHandler.sendToPlayer(new S2CSyncGame2WarehouseMeleePacket(this.extraWarehouseMeleeStacks), serverPlayer);
            }
        }
    }

    private void resetPlayerToDefaultMaxHealth(ServerPlayer player) {
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
        if (player.getHealth() > 20.0F) {
            player.setHealth(20.0F);
        }
        CommonProxy.getXtyCap(player).ifPresent(cap -> {
            cap.setGame2MaxHealth(20.0F);
            cap.setGame2Health(player.getHealth());
        });
    }

    private void clearRoundControlInputs(EndingLibrarySavedData data, ServerPlayer player) {
        data.removeDisabledPermission(player, InputOperations.MOVE_FORWARD);
        data.removeDisabledPermission(player, InputOperations.MOVE_BACKWARD);
        data.removeDisabledPermission(player, InputOperations.MOVE_LEFT);
        data.removeDisabledPermission(player, InputOperations.MOVE_RIGHT);
        data.removeDisabledPermission(player, InputOperations.JUMP);
        data.removeDisabledPermission(player, InputOperations.MOUSE_ATTACK);
        data.removeDisabledPermission(player, InputOperations.MOUSE_USE);
    }

    private void clearRoundArmor(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        Team team = player.getTeam();
        if (team == null) {
            return;
        }
        if (team.getColor() == ChatFormatting.RED || team.getColor() == ChatFormatting.BLUE) {
            player.getInventory().armor.replaceAll(itemStack -> ItemStack.EMPTY);
        }
    }

    private void clearRoundInventory(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        boolean changed = false;
        changed |= clearRoundInventoryList(player.getInventory().items);
        changed |= clearRoundInventoryList(player.getInventory().offhand);
        if (changed) {
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }

    private void clearDeadSavedRoundInventories(Map2SavedData map2SavedData) {
        boolean changed = false;
        for (Map2SavedData.Inventory inventory : map2SavedData.getDeadSavedInventory().values()) {
            changed |= clearRoundInventoryList(inventory.items);
            changed |= clearRoundInventoryList(inventory.offhand);
        }
        if (changed) {
            map2SavedData.setDirty();
        }
    }

    private boolean clearRoundInventoryList(List<ItemStack> items) {
        boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (shouldClearAfterGame2(stack)) {
                items.set(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        return changed;
    }

    private boolean shouldClearAfterGame2(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(ItemInit.C4_BOMB.get())
                || stack.is(ItemInit.BDK.get())
                || stack.getItem() instanceof IGun
                || stack.getItem() instanceof IAmmo
                || stack.getItem() instanceof IAmmoBox
                || stack.getItem() instanceof IMeleeWeapon
                || stack.getItem() instanceof IThrowable
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || this.extraWarehouseMeleeStacks.stream().anyMatch(melee -> ItemStack.isSameItemSameTags(melee, stack));
    }

    private void teleportPlayersToRespawnPoints(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            teleportPlayerToRespawnPoint(player);
        }
    }

    private void teleportPlayerToRespawnPoint(ServerPlayer player) {
        ResourceKey<Level> respawnDimension = player.getRespawnDimension();
        BlockPos respawnPos = player.getRespawnPosition();
        float respawnAngle = player.getRespawnAngle();
        ServerLevel targetLevel = this.server.getLevel(respawnDimension);
        Optional<Vec3> targetPos = Optional.empty();
        if (targetLevel != null && respawnPos != null) {
            targetPos = Player.findRespawnPositionAndUseSpawnBlock(targetLevel, respawnPos, respawnAngle, player.isRespawnForced(), true);
        }
        if (targetLevel == null || targetPos.isEmpty()) {
            targetLevel = this.server.overworld();
            BlockPos sharedSpawn = targetLevel.getSharedSpawnPos();
            respawnAngle = targetLevel.getSharedSpawnAngle();
            targetPos = Optional.of(new Vec3(sharedSpawn.getX() + 0.5D, sharedSpawn.getY(), sharedSpawn.getZ() + 0.5D));
        }
        Vec3 pos = targetPos.get();
        player.teleportTo(targetLevel, pos.x, pos.y, pos.z, respawnAngle, 0.0F);
        while (!targetLevel.noCollision(player) && player.getY() < (double) targetLevel.getMaxBuildHeight()) {
            player.setPos(player.getX(), player.getY() + 1.0D, player.getZ());
        }
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static boolean isValidWarehouseMelee(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    private static boolean sameItemStackList(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.isSameItemSameTags(a.get(i), b.get(i)) || a.get(i).getCount() != b.get(i).getCount()) {
                return false;
            }
        }
        return true;
    }
}
