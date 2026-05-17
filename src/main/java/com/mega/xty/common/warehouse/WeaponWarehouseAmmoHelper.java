package com.mega.xty.common.warehouse;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class WeaponWarehouseAmmoHelper {
    private static final int BACKPACK_SLOT_START = 9;
    private static final int SHOTGUN_AMMO_MULTIPLIER = 3;
    private static final int MIN_SHOTGUN_AMMO_COUNT = 16;
    private static final int DEFAULT_AMMO_MULTIPLIER = 5;
    private static final ResourceLocation GHOST_GUN_ID = ResourceLocation.parse("cataclysm_guns:ghost");

    private WeaponWarehouseAmmoHelper() {
    }

    public static void clearInventoryAmmo(Inventory inventory) {
        clearAmmoList(inventory.items);
        clearAmmoList(inventory.offhand);
    }

    public static void giveAmmoForInventoryGuns(ServerPlayer player) {
        Map<ResourceLocation, Integer> ammoCounts = new LinkedHashMap<>();
        collectGunAmmoRequirements(player.getInventory().items, ammoCounts);
        collectGunAmmoRequirements(player.getInventory().offhand, ammoCounts);
        for (Map.Entry<ResourceLocation, Integer> entry : ammoCounts.entrySet()) {
            giveAmmoToBackpack(player, entry.getKey(), entry.getValue());
        }
    }

    private static void clearAmmoList(NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem() instanceof IAmmo) {
                items.set(i, ItemStack.EMPTY);
            }
        }
    }

    private static void collectGunAmmoRequirements(NonNullList<ItemStack> items, Map<ResourceLocation, Integer> ammoCounts) {
        for (ItemStack stack : items) {
            if (stack.getItem() instanceof IGun gun) {
                TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).ifPresent(index -> {
                    ResourceLocation ammoId = index.getGunData().getAmmoId();
                    if (ammoId == null) {
                        return;
                    }
                    int magazineAmmo = AttachmentDataUtils.getAmmoCountWithAttachment(stack, index.getGunData());
                    if (magazineAmmo <= 0) {
                        return;
                    }
                    boolean shotgun = isShotgun(index.getType()) || isShotgun(index.getPojo().getType());
                    int ammoCount = shotgun ? Math.max(MIN_SHOTGUN_AMMO_COUNT, magazineAmmo * SHOTGUN_AMMO_MULTIPLIER) : magazineAmmo * DEFAULT_AMMO_MULTIPLIER;
                    if (gun.getGunId(stack).equals(GHOST_GUN_ID)) {
                        ammoCount = 3;
                    }
                    ammoCounts.merge(ammoId, ammoCount, Integer::sum);
                });
            }
        }
    }

    private static boolean isShotgun(@Nullable String type) {
        return type != null && GunTabType.SHOTGUN.toString().equalsIgnoreCase(type);
    }

    private static void giveAmmoToBackpack(ServerPlayer player, ResourceLocation ammoId, int count) {
        int remaining = count;
        while (remaining > 0) {
            ItemStack ammo = createAmmoStack(ammoId, 1);
            if (ammo.isEmpty()) {
                return;
            }
            int moveCount = Math.min(remaining, Math.max(1, ammo.getMaxStackSize()));
            ammo.setCount(moveCount);
            ItemStack leftover = insertIntoBackpackSlots(player, ammo);
            if (!leftover.isEmpty()) {
                player.drop(leftover, false);
            }
            remaining -= moveCount;
        }
    }

    private static ItemStack createAmmoStack(ResourceLocation ammoId, int count) {
        ItemStack ammo = ModItems.AMMO.get().getDefaultInstance();
        if (!(ammo.getItem() instanceof IAmmo taczAmmo)) {
            return ItemStack.EMPTY;
        }
        taczAmmo.setAmmoId(ammo, ammoId);
        ammo.setCount(Math.max(1, count));
        return ammo;
    }

    private static ItemStack insertIntoBackpackSlots(ServerPlayer player, ItemStack stack) {
        ItemStack remaining = stack.copy();
        NonNullList<ItemStack> items = player.getInventory().items;
        for (int i = BACKPACK_SLOT_START; i < items.size(); i++) {
            ItemStack existing = items.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining)) {
                int free = existing.getMaxStackSize() - existing.getCount();
                if (free <= 0) {
                    continue;
                }
                int moveCount = Math.min(free, remaining.getCount());
                existing.grow(moveCount);
                remaining.shrink(moveCount);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        for (int i = BACKPACK_SLOT_START; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                ItemStack placed = remaining.copy();
                int moveCount = Math.min(remaining.getCount(), Math.max(1, placed.getMaxStackSize()));
                placed.setCount(moveCount);
                items.set(i, placed);
                remaining.shrink(moveCount);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return remaining;
    }
}
