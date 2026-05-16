package com.mega.xty.common.capability;

import com.mega.endinglib.api.capability.CapabilitySyncType;
import com.mega.endinglib.api.capability.EntitySyncCapabilityBase;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.common.warehouse.WeaponWarehouseItems;
import com.mega.xty.common.warehouse.WeaponWarehouseLoadout;
import com.mega.xty.common.warehouse.WeaponWarehouseSnapshot;
import com.mega.xty.common.warehouse.WeaponWarehouseSlotType;
import com.tacz.guns.api.item.IAmmo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Predicate;

public class WeaponWarehouseCapability extends EntitySyncCapabilityBase {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "weapon_warehouse");
    private WeaponWarehouseSnapshot weaponWarehouse = WeaponWarehouseItems.createDefaultSnapshot();
    private WeaponWarehouseSnapshot clientWeaponWarehouse = WeaponWarehouseItems.createDefaultSnapshot();
    private static final String WEAPON_WAREHOUSE_KEY = "WeaponWarehouse";

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    protected @NotNull Predicate<Entity> canAttach() {
        return entity -> entity instanceof Player;
    }

    @Override
    public void syncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {
        if (dist == Dist.DEDICATED_SERVER && capabilitySyncType == CapabilitySyncType.PLAYER_LOGGED_IN) {
            writeWarehouse(compoundTag, this.weaponWarehouse);
        }
    }

    @Override
    public void readSyncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {
        if (dist == Dist.DEDICATED_SERVER && capabilitySyncType == CapabilitySyncType.PLAYER_LOGGED_IN) {
            WeaponWarehouseSnapshot snapshot = readWarehouse(compoundTag, false);
            if (snapshot != null) {
                setClientWeaponWarehouse(snapshot);
            }
        }
    }

    @Override
    public boolean canSyncWhenTick(Entity entity, Level level) {
        return false;
    }

    @Override
    public void customSerializeNBT(CompoundTag compoundTag) {
        writeWarehouse(compoundTag, this.weaponWarehouse);
    }

    @Override
    public void customDeserializeNBT(CompoundTag compoundTag) {
        WeaponWarehouseSnapshot snapshot = readWarehouse(compoundTag);
        if (snapshot != null) {
            this.weaponWarehouse = snapshot;
            this.clientWeaponWarehouse = snapshot.copy();
        } else {
            this.weaponWarehouse = WeaponWarehouseItems.createDefaultSnapshot();
            this.clientWeaponWarehouse = this.weaponWarehouse.copy();
        }
    }

    public WeaponWarehouseSnapshot getWeaponWarehouse() {
        return this.weaponWarehouse.copy();
    }

    public WeaponWarehouseSnapshot getClientWeaponWarehouse() {
        return this.clientWeaponWarehouse.copy();
    }

    public void setClientWeaponWarehouse(WeaponWarehouseSnapshot snapshot) {
        WeaponWarehouseSnapshot copy = snapshot.copy();
        copy.setSelectedLoadout(WeaponWarehouseItems.clampLoadoutIndex(copy.getSelectedLoadout()));
        this.clientWeaponWarehouse = copy;
    }

    public void setWeaponWarehouse(WeaponWarehouseSnapshot snapshot) {
        WeaponWarehouseSnapshot sanitized = WeaponWarehouseItems.sanitizeSnapshot(snapshot);
        this.weaponWarehouse = sanitized;
        this.clientWeaponWarehouse = sanitized.copy();
    }

    public void setWeaponWarehouse(WeaponWarehouseSnapshot snapshot, Set<ResourceLocation> gunBlacklist) {
        WeaponWarehouseSnapshot sanitized = WeaponWarehouseItems.sanitizeSnapshot(snapshot, gunBlacklist);
        this.weaponWarehouse = sanitized;
        this.clientWeaponWarehouse = sanitized.copy();
    }

    public int getSelectedWarehouseLoadout() {
        return this.weaponWarehouse.getSelectedLoadout();
    }

    public void setSelectedWarehouseLoadout(int index) {
        WeaponWarehouseSnapshot snapshot = this.weaponWarehouse.copy();
        snapshot.setSelectedLoadout(WeaponWarehouseItems.clampLoadoutIndex(index));
        setWeaponWarehouse(snapshot);
    }

    public void applySelectedWarehouseLoadout(Player player) {
        applySelectedWarehouseLoadout(player, Set.of());
    }

    public void applySelectedWarehouseLoadout(Player player, Set<ResourceLocation> gunBlacklist) {
        WeaponWarehouseLoadout loadout = this.weaponWarehouse.getLoadout(this.weaponWarehouse.getSelectedLoadout());
        Inventory inventory = player.getInventory();
        clearItemList(inventory.items);
        clearItemList(inventory.offhand);
        if (player.level() instanceof ServerLevel serverLevel && !Game2SavedData.getInstance(serverLevel.getServer()).isStopped()) {
            if (player.getTeam() != null && player.getTeam().getColor() == ChatFormatting.RED && !player.isCreative()) {
                giveLoadoutItem(player, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.MELEE_WEAPON, loadout.getSlot(WeaponWarehouseSlotType.MELEE_WEAPON.getSlotIndex()), gunBlacklist));
            } else {
                giveLoadoutItem(player, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.MAIN_WEAPON, loadout.getSlot(WeaponWarehouseSlotType.MAIN_WEAPON.getSlotIndex()), gunBlacklist));
                giveLoadoutItem(player, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.SECONDARY_WEAPON, loadout.getSlot(WeaponWarehouseSlotType.SECONDARY_WEAPON.getSlotIndex()), gunBlacklist));
                giveLoadoutItem(player, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.MELEE_WEAPON, loadout.getSlot(WeaponWarehouseSlotType.MELEE_WEAPON.getSlotIndex()), gunBlacklist));
                setOrGiveLoadoutItem(player, 3, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.M67_GRENADE, loadout.getSlot(WeaponWarehouseSlotType.M67_GRENADE.getSlotIndex()), gunBlacklist));
                setOrGiveLoadoutItem(player, 4, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.SMOKE_GRENADE, loadout.getSlot(WeaponWarehouseSlotType.SMOKE_GRENADE.getSlotIndex()), gunBlacklist));
                setOrGiveLoadoutItem(player, 5, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.FLASH_GRENADE, loadout.getSlot(WeaponWarehouseSlotType.FLASH_GRENADE.getSlotIndex()), gunBlacklist));
            }
        } else {
            giveLoadoutItem(player, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.MAIN_WEAPON, loadout.getSlot(WeaponWarehouseSlotType.MAIN_WEAPON.getSlotIndex()), gunBlacklist));
            giveLoadoutItem(player, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.SECONDARY_WEAPON, loadout.getSlot(WeaponWarehouseSlotType.SECONDARY_WEAPON.getSlotIndex()), gunBlacklist));
            giveLoadoutItem(player, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.MELEE_WEAPON, loadout.getSlot(WeaponWarehouseSlotType.MELEE_WEAPON.getSlotIndex()), gunBlacklist));
            setOrGiveLoadoutItem(player, 3, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.M67_GRENADE, loadout.getSlot(WeaponWarehouseSlotType.M67_GRENADE.getSlotIndex()), gunBlacklist));
            setOrGiveLoadoutItem(player, 4, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.SMOKE_GRENADE, loadout.getSlot(WeaponWarehouseSlotType.SMOKE_GRENADE.getSlotIndex()), gunBlacklist));
            setOrGiveLoadoutItem(player, 5, WeaponWarehouseItems.sanitizeSlot(WeaponWarehouseSlotType.FLASH_GRENADE, loadout.getSlot(WeaponWarehouseSlotType.FLASH_GRENADE.getSlotIndex()), gunBlacklist));
        }
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.MAINHAND, inventory.getItem(inventory.selected));
        inventory.setChanged();
        if (!player.level().isClientSide) {
            player.containerMenu.broadcastChanges();
        }
    }

    public void applySelectedWarehouseMelee(Player player) {
        WeaponWarehouseLoadout loadout = this.weaponWarehouse.getLoadout(this.weaponWarehouse.getSelectedLoadout());
        giveLoadoutItem(player, loadout.getSlot(WeaponWarehouseSlotType.MELEE_WEAPON.getSlotIndex()));
        if (!player.level().isClientSide) {
            player.containerMenu.broadcastChanges();
        }
    }

    private static void clearItemList(NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!(stack.getItem() instanceof IAmmo) && !stack.is(ItemInit.C4_BOMB.get()) && !stack.is(ItemInit.BDK.get())) {
                items.set(i, ItemStack.EMPTY);
            }
        }
    }

    private static void giveLoadoutItem(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemStack copy = stack.copy();
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }
    }

    private static void setOrGiveLoadoutItem(Player player, int slotIndex, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        Inventory inventory = player.getInventory();
        ItemStack copy = stack.copy();
        if (inventory.getItem(slotIndex).isEmpty()) {
            inventory.setItem(slotIndex, copy);
        } else if (!inventory.add(copy)) {
            player.drop(copy, false);
        }
    }

    private static void writeWarehouse(CompoundTag compoundTag, WeaponWarehouseSnapshot snapshot) {
        compoundTag.put(WEAPON_WAREHOUSE_KEY, snapshot.save());
    }

    private static WeaponWarehouseSnapshot readWarehouse(CompoundTag compoundTag) {
        return readWarehouse(compoundTag, true);
    }

    private static WeaponWarehouseSnapshot readWarehouse(CompoundTag compoundTag, boolean sanitize) {
        if (!compoundTag.contains(WEAPON_WAREHOUSE_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        WeaponWarehouseSnapshot snapshot = WeaponWarehouseSnapshot.load(compoundTag.getCompound(WEAPON_WAREHOUSE_KEY));
        return sanitize ? WeaponWarehouseItems.sanitizeSnapshot(snapshot) : snapshot;
    }
}
