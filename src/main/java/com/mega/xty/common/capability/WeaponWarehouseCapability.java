package com.mega.xty.common.capability;

import com.mega.endinglib.api.capability.CapabilitySyncType;
import com.mega.endinglib.api.capability.EntitySyncCapabilityBase;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.warehouse.WeaponWarehouseItems;
import com.mega.xty.common.warehouse.WeaponWarehouseLoadout;
import com.mega.xty.common.warehouse.WeaponWarehouseSnapshot;
import com.mega.xty.common.warehouse.WeaponWarehouseSlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;

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

    public int getSelectedWarehouseLoadout() {
        return this.weaponWarehouse.getSelectedLoadout();
    }

    public void setSelectedWarehouseLoadout(int index) {
        WeaponWarehouseSnapshot snapshot = this.weaponWarehouse.copy();
        snapshot.setSelectedLoadout(WeaponWarehouseItems.clampLoadoutIndex(index));
        setWeaponWarehouse(snapshot);
    }

    public void applySelectedWarehouseLoadout(Player player) {
        WeaponWarehouseLoadout loadout = this.weaponWarehouse.getLoadout(this.weaponWarehouse.getSelectedLoadout());
        Inventory inventory = player.getInventory();
        clearItemList(inventory.items);
        clearItemList(inventory.offhand);
        if (player.getTeam() != null && player.getTeam().getColor() == ChatFormatting.RED && !player.isCreative()) {
            inventory.items.set(0, loadout.getSlot(WeaponWarehouseSlotType.MELEE_WEAPON.getSlotIndex()).copy());
        } else {
            inventory.items.set(0, loadout.getSlot(WeaponWarehouseSlotType.MAIN_WEAPON.getSlotIndex()).copy());
            inventory.items.set(1, loadout.getSlot(WeaponWarehouseSlotType.SECONDARY_WEAPON.getSlotIndex()).copy());
            inventory.items.set(2, loadout.getSlot(WeaponWarehouseSlotType.MELEE_WEAPON.getSlotIndex()).copy());
            inventory.items.set(3, loadout.getSlot(WeaponWarehouseSlotType.M67_GRENADE.getSlotIndex()).copy());
            inventory.items.set(4, loadout.getSlot(WeaponWarehouseSlotType.SMOKE_GRENADE.getSlotIndex()).copy());
            inventory.items.set(5, loadout.getSlot(WeaponWarehouseSlotType.FLASH_GRENADE.getSlotIndex()).copy());
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
        Inventory inventory = player.getInventory();
        inventory.items.set(0, loadout.getSlot(WeaponWarehouseSlotType.MELEE_WEAPON.getSlotIndex()).copy());
        if (!player.level().isClientSide) {
            player.containerMenu.broadcastChanges();
        }
    }

    private static void clearItemList(NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
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
