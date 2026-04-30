package com.mega.xty.common.warehouse;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import com.mega.xty.common.data.map2.ClientGame2Data;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class WeaponWarehouseItems {
    public static final int LOADOUT_COUNT = 5;
    public static final int SLOT_COUNT = 6;
    public static final ResourceLocation DEFAULT_MELEE_ID = ResourceLocation.fromNamespaceAndPath("lrtactical", "karambit");
    public static final ResourceLocation M67_ID = ResourceLocation.fromNamespaceAndPath("lrtactical", "m67");
    public static final ResourceLocation SMOKE_ID = ResourceLocation.fromNamespaceAndPath("lrtactical", "smoke_grenade");
    public static final ResourceLocation FLASH_ID = ResourceLocation.fromNamespaceAndPath("lrtactical", "flash_grenade");
    private static final Comparator<Map.Entry<ResourceLocation, ?>> RESOURCE_ID_COMPARATOR = Comparator.comparing(entry -> entry.getKey().toString());

    private WeaponWarehouseItems() {
    }

    public static WeaponWarehouseSnapshot createDefaultSnapshot() {
        WeaponWarehouseSnapshot snapshot = new WeaponWarehouseSnapshot();
        snapshot.setSelectedLoadout(0);
        for (int i = 0; i < LOADOUT_COUNT; i++) {
            snapshot.getLoadout(i).setSlot(WeaponWarehouseSlotType.MELEE_WEAPON.getSlotIndex(), createDefaultMeleeStack());
        }
        return snapshot;
    }

    public static ItemStack createDefaultMeleeStack() {
        return createMeleeStack(DEFAULT_MELEE_ID);
    }

    public static ItemStack createMeleeStack(ResourceLocation meleeId) {
        ItemStack stack = me.xjqsh.lrtactical.init.ModItems.MELEE.get().getDefaultInstance();
        IMeleeWeapon.of(stack).setId(stack, meleeId);
        return stack;
    }

    public static ItemStack createThrowableStack(ResourceLocation throwableId, int count) {
        ItemStack stack = me.xjqsh.lrtactical.init.ModItems.THROWABLE.get().getDefaultInstance();
        IThrowable.of(stack).setId(stack, throwableId);
        stack.setCount(Math.max(1, count));
        return stack;
    }

    public static ItemStack createGunStack(ResourceLocation gunId) {
        return TimelessAPI.getCommonGunIndex(gunId)
                .map(index -> {
                    ItemStack stack = new ItemStack(com.tacz.guns.init.ModItems.MODERN_KINETIC_GUN.get());
                    if (stack.getItem() instanceof IGun gun) {
                        gun.setGunId(stack, gunId);
                        gun.setFireMode(stack, resolveDefaultFireMode(index.getGunData().getFireModeSet()));
                        gun.setCurrentAmmoCount(stack, Math.max(0, index.getGunData().getAmmoAmount()));
                        gun.setBulletInBarrel(stack, false);
                        return stack;
                    }
                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }

    public static WeaponWarehouseSnapshot sanitizeSnapshot(WeaponWarehouseSnapshot snapshot) {
        WeaponWarehouseSnapshot sanitized = createDefaultSnapshot();
        sanitized.setSelectedLoadout(clampLoadoutIndex(snapshot.getSelectedLoadout()));
        for (int loadout = 0; loadout < LOADOUT_COUNT; loadout++) {
            WeaponWarehouseLoadout source = snapshot.getLoadout(loadout);
            WeaponWarehouseLoadout target = sanitized.getLoadout(loadout);
            for (WeaponWarehouseSlotType slotType : WeaponWarehouseSlotType.values()) {
                target.setSlot(slotType.getSlotIndex(), sanitizeSlot(slotType, source.getSlot(slotType.getSlotIndex())));
            }
        }
        return sanitized;
    }

    public static ItemStack sanitizeSlot(WeaponWarehouseSlotType slotType, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack copy = stack.copy();
        copy.setCount(Math.max(1, copy.getCount()));
        return switch (slotType) {
            case MAIN_WEAPON -> isMainWeapon(copy) ? forceSingleCount(copy) : ItemStack.EMPTY;
            case SECONDARY_WEAPON -> isSecondaryWeapon(copy) ? forceSingleCount(copy) : ItemStack.EMPTY;
            case MELEE_WEAPON -> forceSingleCount(copy);
            case M67_GRENADE -> sanitizeFixedThrowable(copy, M67_ID);
            case SMOKE_GRENADE -> sanitizeFixedThrowable(copy, SMOKE_ID);
            case FLASH_GRENADE -> sanitizeFixedThrowable(copy, FLASH_ID);
        };
    }

    private static ItemStack sanitizeFixedThrowable(ItemStack stack, ResourceLocation expectedId) {
        if (!isFixedThrowable(stack, expectedId)) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    public static int clampLoadoutIndex(int index) {
        if (index < 0) {
            return 0;
        }
        return Math.min(index, LOADOUT_COUNT - 1);
    }

    public static boolean isMainWeapon(ItemStack stack) {
        if (!(stack.getItem() instanceof IGun gun)) {
            return false;
        }
        return TimelessAPI.getCommonGunIndex(gun.getGunId(stack))
                .map(index -> !"pistol".equalsIgnoreCase(index.getPojo().getType()))
                .orElse(false);
    }
    public static boolean isSecondaryWeapon(ItemStack stack) {
        if (!(stack.getItem() instanceof IGun gun)) {
            return false;
        }
        return TimelessAPI.getCommonGunIndex(gun.getGunId(stack))
                .map(index -> "pistol".equalsIgnoreCase(index.getPojo().getType()))
                .orElse(false);
    }

    public static boolean isMeleeWeapon(ItemStack stack) {
        return stack.getItem() instanceof IMeleeWeapon meleeWeapon && meleeWeapon.getMeleeIndex(stack).isPresent();
    }

    public static boolean isFixedThrowable(ItemStack stack, ResourceLocation expectedId) {
        return stack.getItem() instanceof IThrowable throwable && expectedId.equals(throwable.getId(stack));
    }

    public static List<ItemStack> createClientCandidates(WeaponWarehouseSlotType slotType) {
        return switch (slotType) {
            case MAIN_WEAPON -> createClientGunCandidates(index -> !"pistol".equalsIgnoreCase(index.getType()));
            case SECONDARY_WEAPON -> createClientGunCandidates(index -> "pistol".equalsIgnoreCase(index.getType()));
            case MELEE_WEAPON -> createClientMeleeCandidates();
            case M67_GRENADE -> createThrowableCountCandidates(M67_ID);
            case SMOKE_GRENADE -> createThrowableCountCandidates(SMOKE_ID);
            case FLASH_GRENADE -> createThrowableCountCandidates(FLASH_ID);
        };
    }

    private static List<ItemStack> createClientGunCandidates(Predicate<com.tacz.guns.client.resource.index.ClientGunIndex> filter) {
        List<Map.Entry<ResourceLocation, com.tacz.guns.client.resource.index.ClientGunIndex>> entries = new ArrayList<>(TimelessAPI.getAllClientGunIndex());
        entries.sort(RESOURCE_ID_COMPARATOR);
        List<ItemStack> items = new ArrayList<>();
        items.add(ItemStack.EMPTY);
        LinkedHashSet<ResourceLocation> seen = new LinkedHashSet<>();
        for (Map.Entry<ResourceLocation, com.tacz.guns.client.resource.index.ClientGunIndex> entry : entries) {
            if (filter.test(entry.getValue()) && seen.add(entry.getKey())) {
                ItemStack stack = createGunStack(entry.getKey());
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }
        return items;
    }

    private static List<ItemStack> createClientMeleeCandidates() {
        List<ItemStack> items = new ArrayList<>();
        items.add(ItemStack.EMPTY);
        LinkedHashSet<ResourceLocation> seen = new LinkedHashSet<>();
        List<me.xjqsh.lrtactical.item.index.MeleeWeaponIndex<?>> indexes = new ArrayList<>(LrTacticalAPI.getMeleeIndexes());
        indexes.sort(Comparator.comparing(index -> index.getId().toString()));
        for (me.xjqsh.lrtactical.item.index.MeleeWeaponIndex<?> index : indexes) {
            ResourceLocation meleeId = index.getId();
            if (!seen.add(meleeId)) {
                continue;
            }
            ItemStack stack = createMeleeStack(meleeId);
            if (isMeleeWeapon(stack)) {
                items.add(stack);
            }
        }
        for (ItemStack customStack : ClientGame2Data.extraWarehouseMeleeStacks) {
            if (customStack.isEmpty() || items.stream().anyMatch(existing -> ItemStack.isSameItemSameTags(existing, customStack))) {
                continue;
            }
            items.add(customStack.copy());
        }
        return items;
    }

    public static List<ItemStack> createThrowableCountCandidates(ResourceLocation throwableId) {
        List<ItemStack> items = new ArrayList<>();
        items.add(ItemStack.EMPTY);
        ItemStack base = createThrowableStack(throwableId, 1);
        if (base.getItem() instanceof IThrowable throwable && throwable.getThrowableIndex(base).isPresent()) {
            items.add(base);
        }
        return items;
    }

    public static String createSearchText(ItemStack stack) {
        if (stack.isEmpty()) {
            return "empty none";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(stack.getHoverName().getString().toLowerCase(Locale.ROOT));
        if (stack.getItem() instanceof IGun gun) {
            builder.append(' ').append(gun.getGunId(stack));
            TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).ifPresent(index -> builder.append(' ').append(index.getPojo().getType()));
        } else if (stack.getItem() instanceof IMeleeWeapon meleeWeapon) {
            Optional.ofNullable(meleeWeapon.getId(stack)).ifPresent(id -> builder.append(' ').append(id));
        } else if (stack.getItem() instanceof IThrowable throwable) {
            builder.append(' ').append(throwable.getId(stack));
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    public static String getDisplayName(ItemStack stack) {
        if (stack.isEmpty()) {
            return I18n.get("screen.xtymegamod.weapon_warehouse.empty");
        }
        return stack.getHoverName().getString();
    }

    private static ItemStack forceSingleCount(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static FireMode resolveDefaultFireMode(List<FireMode> modes) {
        if (modes == null || modes.isEmpty()) {
            return FireMode.SEMI;
        }
        if (modes.contains(FireMode.AUTO)) {
            return FireMode.AUTO;
        }
        if (modes.contains(FireMode.SEMI)) {
            return FireMode.SEMI;
        }
        if (modes.contains(FireMode.BURST)) {
            return FireMode.BURST;
        }
        return modes.get(0);
    }
}
