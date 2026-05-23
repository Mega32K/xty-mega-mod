package com.mega.map.common.warehouse;

public enum WeaponWarehouseSlotType {
    MAIN_WEAPON(0, "screen.megamod.weapon_warehouse.slot.main"),
    SECONDARY_WEAPON(1, "screen.megamod.weapon_warehouse.slot.secondary"),
    MELEE_WEAPON(2, "screen.megamod.weapon_warehouse.slot.melee"),
    M67_GRENADE(3, "screen.megamod.weapon_warehouse.slot.m67"),
    SMOKE_GRENADE(4, "screen.megamod.weapon_warehouse.slot.smoke"),
    FLASH_GRENADE(5, "screen.megamod.weapon_warehouse.slot.flash");

    private final int slotIndex;
    private final String translationKey;

    WeaponWarehouseSlotType(int slotIndex, String translationKey) {
        this.slotIndex = slotIndex;
        this.translationKey = translationKey;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public static WeaponWarehouseSlotType bySlot(int slot) {
        for (WeaponWarehouseSlotType type : values()) {
            if (type.slotIndex == slot) {
                return type;
            }
        }
        throw new IndexOutOfBoundsException("Weapon warehouse slot is out of range: " + slot);
    }
}
