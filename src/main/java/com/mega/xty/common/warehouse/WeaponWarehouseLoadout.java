package com.mega.xty.common.warehouse;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class WeaponWarehouseLoadout {
    private final NonNullList<ItemStack> slots = NonNullList.withSize(WeaponWarehouseItems.SLOT_COUNT, ItemStack.EMPTY);

    public ItemStack getSlot(int slot) {
        return this.slots.get(slot);
    }

    public void setSlot(int slot, ItemStack stack) {
        this.slots.set(slot, stack.copy());
    }

    public NonNullList<ItemStack> getSlots() {
        return this.slots;
    }

    public WeaponWarehouseLoadout copy() {
        WeaponWarehouseLoadout copy = new WeaponWarehouseLoadout();
        for (int i = 0; i < this.slots.size(); i++) {
            copy.setSlot(i, this.slots.get(i));
        }
        return copy;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (ItemStack stack : this.slots) {
            listTag.add(stack.save(new CompoundTag()));
        }
        tag.put("Slots", listTag);
        return tag;
    }

    public static WeaponWarehouseLoadout load(CompoundTag tag) {
        WeaponWarehouseLoadout loadout = new WeaponWarehouseLoadout();
        if (tag.contains("Slots", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("Slots", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size() && i < WeaponWarehouseItems.SLOT_COUNT; i++) {
                loadout.setSlot(i, ItemStack.of(listTag.getCompound(i)));
            }
        }
        return loadout;
    }
}
