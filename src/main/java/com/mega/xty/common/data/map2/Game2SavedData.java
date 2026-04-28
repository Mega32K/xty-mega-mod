package com.mega.xty.common.data.map2;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.common.data.EndingLibrarySavedData;
import com.mega.endinglib.common.data.InputOperations;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.game2.S2CGame2StatsPacket;
import com.mega.xty.common.network.s2c.map2.game2.S2CSyncGame2WarehouseMeleePacket;
import com.mega.xty.proxy.CommonProxy;
import com.tacz.guns.api.item.IGun;
import com.mega.xty.util.data_expand.SavedDataGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Game2SavedData extends SavedData {
    private static final String EXTRA_WAREHOUSE_MELEE_KEY = "ExtraWarehouseMelee";
    public MinecraftServer server;
    private boolean isStopped = true;
    private Game2Functions game2Functions = new Game2Functions(this);
    private List<ItemStack> extraWarehouseMeleeStacks = new ArrayList<>();
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
        return tag;
    }

    public void setStopped(boolean stopped) {
        if (this.isStopped != stopped) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                NetworkHandler.sendToPlayer(new S2CGame2StatsPacket(stopped), serverPlayer);
                NetworkHandler.sendToPlayer(new S2CSyncGame2WarehouseMeleePacket(this.extraWarehouseMeleeStacks), serverPlayer);
            }
        }
        if (stopped) {
            FpsSavedData fpsSavedData = FpsSavedData.getInstance(server);
            EndingLibrarySavedData elData = EndingLibrarySavedData.getInstance(this.server);
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
        }
        isStopped = stopped;
    }
    public boolean isStopped() {
        return isStopped;
    }
    public Game2Functions getGame2Functions() {
        return game2Functions;
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
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (shouldClearAfterGame2(stack)) {
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack stack = player.getInventory().offhand.get(i);
            if (shouldClearAfterGame2(stack)) {
                player.getInventory().offhand.set(i, ItemStack.EMPTY);
            }
        }
    }

    private boolean shouldClearAfterGame2(ItemStack stack) {
        return stack.getItem() instanceof IGun || stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
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
