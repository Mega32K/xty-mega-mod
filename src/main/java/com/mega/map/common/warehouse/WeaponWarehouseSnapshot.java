package com.mega.map.common.warehouse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class WeaponWarehouseSnapshot {
    private int selectedLoadout;
    private final List<WeaponWarehouseLoadout> loadouts;

    public WeaponWarehouseSnapshot() {
        this.loadouts = new ArrayList<>(WeaponWarehouseItems.LOADOUT_COUNT);
        for (int i = 0; i < WeaponWarehouseItems.LOADOUT_COUNT; i++) {
            this.loadouts.add(new WeaponWarehouseLoadout());
        }
    }

    public int getSelectedLoadout() {
        return this.selectedLoadout;
    }

    public void setSelectedLoadout(int selectedLoadout) {
        this.selectedLoadout = selectedLoadout;
    }

    public WeaponWarehouseLoadout getLoadout(int index) {
        return this.loadouts.get(index);
    }

    public List<WeaponWarehouseLoadout> getLoadouts() {
        return this.loadouts;
    }

    public WeaponWarehouseSnapshot copy() {
        WeaponWarehouseSnapshot copy = new WeaponWarehouseSnapshot();
        copy.selectedLoadout = this.selectedLoadout;
        for (int i = 0; i < this.loadouts.size(); i++) {
            copy.loadouts.set(i, this.loadouts.get(i).copy());
        }
        return copy;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SelectedLoadout", this.selectedLoadout);
        ListTag listTag = new ListTag();
        for (WeaponWarehouseLoadout loadout : this.loadouts) {
            listTag.add(loadout.save());
        }
        tag.put("Loadouts", listTag);
        return tag;
    }

    public static WeaponWarehouseSnapshot load(CompoundTag tag) {
        WeaponWarehouseSnapshot snapshot = new WeaponWarehouseSnapshot();
        snapshot.selectedLoadout = tag.getInt("SelectedLoadout");
        if (tag.contains("Loadouts", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("Loadouts", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size() && i < WeaponWarehouseItems.LOADOUT_COUNT; i++) {
                snapshot.loadouts.set(i, WeaponWarehouseLoadout.load(listTag.getCompound(i)));
            }
        }
        return snapshot;
    }
}
