package com.mega.map.common.options.map2game2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class Game2ServerOptions {
    private static final String MATCH_KEY = "Match";
    private static final String BOMB_KEY = "Bomb";
    private static final String LOADOUT_KEY = "Loadout";

    private final Match match = new Match();
    private final Bomb bomb = new Bomb();
    private final Loadout loadout = new Loadout();

    public Match getMatch() {
        return this.match;
    }

    public Bomb getBomb() {
        return this.bomb;
    }

    public Loadout getLoadout() {
        return this.loadout;
    }

    public void copyFrom(Game2ServerOptions other) {
        this.match.copyFrom(other.match);
        this.bomb.copyFrom(other.bomb);
        this.loadout.copyFrom(other.loadout);
    }

    public CompoundTag save(CompoundTag tag) {
        tag.put(MATCH_KEY, this.match.save(new CompoundTag()));
        tag.put(BOMB_KEY, this.bomb.save(new CompoundTag()));
        tag.put(LOADOUT_KEY, this.loadout.save(new CompoundTag()));
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains(MATCH_KEY, Tag.TAG_COMPOUND)) {
            this.match.load(tag.getCompound(MATCH_KEY));
        }
        if (tag.contains(BOMB_KEY, Tag.TAG_COMPOUND)) {
            this.bomb.load(tag.getCompound(BOMB_KEY));
        }
        if (tag.contains(LOADOUT_KEY, Tag.TAG_COMPOUND)) {
            this.loadout.load(tag.getCompound(LOADOUT_KEY));
        }
    }

    public static final class Match {
        public static final int DEFAULT_MAX_WINS = 0;
        public static final int DEFAULT_ROUND_START_LOCK_TICKS = 10 * 20;
        public static final int DEFAULT_NEXT_ROUND_DELAY_TICKS = 7 * 20;
        public static final int DEFAULT_ROUND_START_HEALTH = 100;

        private int maxWins = DEFAULT_MAX_WINS;
        private boolean maxWinsLoaded;
        private int roundStartLockTicks = DEFAULT_ROUND_START_LOCK_TICKS;
        private int nextRoundDelayTicks = DEFAULT_NEXT_ROUND_DELAY_TICKS;
        private int roundStartHealth = DEFAULT_ROUND_START_HEALTH;

        public int getMaxWins() {
            return this.maxWins;
        }

        public void setMaxWins(int maxWins) {
            this.maxWins = Math.max(0, maxWins);
            this.maxWinsLoaded = true;
        }

        public boolean hasLoadedMaxWins() {
            return this.maxWinsLoaded;
        }

        public int getRoundStartLockTicks() {
            return this.roundStartLockTicks;
        }

        public void setRoundStartLockTicks(int roundStartLockTicks) {
            this.roundStartLockTicks = Math.max(20, roundStartLockTicks);
        }

        public int getNextRoundDelayTicks() {
            return this.nextRoundDelayTicks;
        }

        public void setNextRoundDelayTicks(int nextRoundDelayTicks) {
            this.nextRoundDelayTicks = Math.max(20, nextRoundDelayTicks);
        }

        public int getRoundStartHealth() {
            return this.roundStartHealth;
        }

        public void setRoundStartHealth(int roundStartHealth) {
            this.roundStartHealth = Math.max(1, roundStartHealth);
        }

        private void copyFrom(Match other) {
            this.maxWins = other.maxWins;
            this.maxWinsLoaded = other.maxWinsLoaded;
            this.roundStartLockTicks = other.roundStartLockTicks;
            this.nextRoundDelayTicks = other.nextRoundDelayTicks;
            this.roundStartHealth = other.roundStartHealth;
        }

        private CompoundTag save(CompoundTag tag) {
            tag.putInt("maxWins", this.maxWins);
            tag.putInt("roundStartLockTicks", this.roundStartLockTicks);
            tag.putInt("nextRoundDelayTicks", this.nextRoundDelayTicks);
            tag.putInt("roundStartHealth", this.roundStartHealth);
            return tag;
        }

        private void load(CompoundTag tag) {
            if (tag.contains("maxWins", Tag.TAG_INT)) {
                this.maxWins = Math.max(0, tag.getInt("maxWins"));
                this.maxWinsLoaded = true;
            }
            if (tag.contains("roundStartLockTicks", Tag.TAG_INT)) {
                this.roundStartLockTicks = Math.max(20, tag.getInt("roundStartLockTicks"));
            }
            if (tag.contains("nextRoundDelayTicks", Tag.TAG_INT)) {
                this.nextRoundDelayTicks = Math.max(20, tag.getInt("nextRoundDelayTicks"));
            }
            if (tag.contains("roundStartHealth", Tag.TAG_INT)) {
                this.roundStartHealth = Math.max(1, tag.getInt("roundStartHealth"));
            }
        }
    }

    public static final class Bomb {
        public static final int DEFAULT_COUNTDOWN_TICKS = 40 * 20;
        public static final int DEFAULT_PLANT_DURATION_TICKS = 4 * 20;
        public static final int DEFAULT_DEFUSE_DURATION_TICKS = 4 * 20;
        public static final double DEFAULT_PLANT_SITE_DISTANCE = 6.0D;
        public static final double DEFAULT_DEFUSE_DISTANCE = 0.7D;

        private int countdownTicks = DEFAULT_COUNTDOWN_TICKS;
        private int plantDurationTicks = DEFAULT_PLANT_DURATION_TICKS;
        private int defuseDurationTicks = DEFAULT_DEFUSE_DURATION_TICKS;
        private double plantSiteDistance = DEFAULT_PLANT_SITE_DISTANCE;
        private double defuseDistance = DEFAULT_DEFUSE_DISTANCE;

        public int getCountdownTicks() {
            return this.countdownTicks;
        }

        public void setCountdownTicks(int countdownTicks) {
            this.countdownTicks = Math.max(20, countdownTicks);
        }

        public int getPlantDurationTicks() {
            return this.plantDurationTicks;
        }

        public void setPlantDurationTicks(int plantDurationTicks) {
            this.plantDurationTicks = Math.max(20, plantDurationTicks);
        }

        public int getDefuseDurationTicks() {
            return this.defuseDurationTicks;
        }

        public void setDefuseDurationTicks(int defuseDurationTicks) {
            this.defuseDurationTicks = Math.max(20, defuseDurationTicks);
        }

        public double getPlantSiteDistance() {
            return this.plantSiteDistance;
        }

        public void setPlantSiteDistance(double plantSiteDistance) {
            this.plantSiteDistance = Math.max(0.5D, plantSiteDistance);
        }

        public double getDefuseDistance() {
            return this.defuseDistance;
        }

        public void setDefuseDistance(double defuseDistance) {
            this.defuseDistance = Math.max(0.1D, defuseDistance);
        }

        private void copyFrom(Bomb other) {
            this.countdownTicks = other.countdownTicks;
            this.plantDurationTicks = other.plantDurationTicks;
            this.defuseDurationTicks = other.defuseDurationTicks;
            this.plantSiteDistance = other.plantSiteDistance;
            this.defuseDistance = other.defuseDistance;
        }

        private CompoundTag save(CompoundTag tag) {
            tag.putInt("countdownTicks", this.countdownTicks);
            tag.putInt("plantDurationTicks", this.plantDurationTicks);
            tag.putInt("defuseDurationTicks", this.defuseDurationTicks);
            tag.putDouble("plantSiteDistance", this.plantSiteDistance);
            tag.putDouble("defuseDistance", this.defuseDistance);
            return tag;
        }

        private void load(CompoundTag tag) {
            if (tag.contains("countdownTicks", Tag.TAG_INT)) {
                this.countdownTicks = Math.max(20, tag.getInt("countdownTicks"));
            }
            if (tag.contains("plantDurationTicks", Tag.TAG_INT)) {
                this.plantDurationTicks = Math.max(20, tag.getInt("plantDurationTicks"));
            }
            if (tag.contains("defuseDurationTicks", Tag.TAG_INT)) {
                this.defuseDurationTicks = Math.max(20, tag.getInt("defuseDurationTicks"));
            }
            if (tag.contains("plantSiteDistance", Tag.TAG_DOUBLE)) {
                this.plantSiteDistance = Math.max(0.5D, tag.getDouble("plantSiteDistance"));
            }
            if (tag.contains("defuseDistance", Tag.TAG_DOUBLE)) {
                this.defuseDistance = Math.max(0.1D, tag.getDouble("defuseDistance"));
            }
        }
    }

    public static final class Loadout {
        public static final boolean DEFAULT_CLEAR_DROPPED_ITEMS_ON_NEW_ROUND = true;
        public static final boolean DEFAULT_GIVE_BLUE_DEFUSE_KIT = true;
        public static final boolean DEFAULT_GIVE_BLUE_CREATIVE_AMMO_BOX = true;
        public static final boolean DEFAULT_FILL_BLUE_GUN_AMMO = true;
        public static final boolean DEFAULT_GIVE_RED_BOMB = true;
        public static final boolean DEFAULT_EQUIP_RED_NANOSUIT = true;
        public static final int DEFAULT_BLUE_ARMOR_DURABILITY = 10;

        private boolean clearDroppedItemsOnNewRound = DEFAULT_CLEAR_DROPPED_ITEMS_ON_NEW_ROUND;
        private boolean giveBlueDefuseKit = DEFAULT_GIVE_BLUE_DEFUSE_KIT;
        private boolean giveBlueCreativeAmmoBox = DEFAULT_GIVE_BLUE_CREATIVE_AMMO_BOX;
        private boolean fillBlueGunAmmo = DEFAULT_FILL_BLUE_GUN_AMMO;
        private boolean giveRedBomb = DEFAULT_GIVE_RED_BOMB;
        private boolean equipRedNanosuit = DEFAULT_EQUIP_RED_NANOSUIT;
        private int blueArmorDurability = DEFAULT_BLUE_ARMOR_DURABILITY;

        public boolean isClearDroppedItemsOnNewRound() {
            return this.clearDroppedItemsOnNewRound;
        }

        public void setClearDroppedItemsOnNewRound(boolean clearDroppedItemsOnNewRound) {
            this.clearDroppedItemsOnNewRound = clearDroppedItemsOnNewRound;
        }

        public boolean isGiveBlueDefuseKit() {
            return this.giveBlueDefuseKit;
        }

        public void setGiveBlueDefuseKit(boolean giveBlueDefuseKit) {
            this.giveBlueDefuseKit = giveBlueDefuseKit;
        }

        public boolean isGiveBlueCreativeAmmoBox() {
            return this.giveBlueCreativeAmmoBox;
        }

        public void setGiveBlueCreativeAmmoBox(boolean giveBlueCreativeAmmoBox) {
            this.giveBlueCreativeAmmoBox = giveBlueCreativeAmmoBox;
        }

        public boolean isFillBlueGunAmmo() {
            return this.fillBlueGunAmmo;
        }

        public void setFillBlueGunAmmo(boolean fillBlueGunAmmo) {
            this.fillBlueGunAmmo = fillBlueGunAmmo;
        }

        public boolean isGiveRedBomb() {
            return this.giveRedBomb;
        }

        public void setGiveRedBomb(boolean giveRedBomb) {
            this.giveRedBomb = giveRedBomb;
        }

        public boolean isEquipRedNanosuit() {
            return this.equipRedNanosuit;
        }

        public void setEquipRedNanosuit(boolean equipRedNanosuit) {
            this.equipRedNanosuit = equipRedNanosuit;
        }

        public int getBlueArmorDurability() {
            return this.blueArmorDurability;
        }

        public void setBlueArmorDurability(int blueArmorDurability) {
            this.blueArmorDurability = Math.max(0, blueArmorDurability);
        }

        private void copyFrom(Loadout other) {
            this.clearDroppedItemsOnNewRound = other.clearDroppedItemsOnNewRound;
            this.giveBlueDefuseKit = other.giveBlueDefuseKit;
            this.giveBlueCreativeAmmoBox = other.giveBlueCreativeAmmoBox;
            this.fillBlueGunAmmo = other.fillBlueGunAmmo;
            this.giveRedBomb = other.giveRedBomb;
            this.equipRedNanosuit = other.equipRedNanosuit;
            this.blueArmorDurability = other.blueArmorDurability;
        }

        private CompoundTag save(CompoundTag tag) {
            tag.putBoolean("clearDroppedItemsOnNewRound", this.clearDroppedItemsOnNewRound);
            tag.putBoolean("giveBlueDefuseKit", this.giveBlueDefuseKit);
            tag.putBoolean("giveBlueCreativeAmmoBox", this.giveBlueCreativeAmmoBox);
            tag.putBoolean("fillBlueGunAmmo", this.fillBlueGunAmmo);
            tag.putBoolean("giveRedBomb", this.giveRedBomb);
            tag.putBoolean("equipRedNanosuit", this.equipRedNanosuit);
            tag.putInt("blueArmorDurability", this.blueArmorDurability);
            return tag;
        }

        private void load(CompoundTag tag) {
            if (tag.contains("clearDroppedItemsOnNewRound", Tag.TAG_BYTE)) {
                this.clearDroppedItemsOnNewRound = tag.getBoolean("clearDroppedItemsOnNewRound");
            }
            if (tag.contains("giveBlueDefuseKit", Tag.TAG_BYTE)) {
                this.giveBlueDefuseKit = tag.getBoolean("giveBlueDefuseKit");
            }
            if (tag.contains("giveBlueCreativeAmmoBox", Tag.TAG_BYTE)) {
                this.giveBlueCreativeAmmoBox = tag.getBoolean("giveBlueCreativeAmmoBox");
            }
            if (tag.contains("fillBlueGunAmmo", Tag.TAG_BYTE)) {
                this.fillBlueGunAmmo = tag.getBoolean("fillBlueGunAmmo");
            }
            if (tag.contains("giveRedBomb", Tag.TAG_BYTE)) {
                this.giveRedBomb = tag.getBoolean("giveRedBomb");
            }
            if (tag.contains("equipRedNanosuit", Tag.TAG_BYTE)) {
                this.equipRedNanosuit = tag.getBoolean("equipRedNanosuit");
            }
            if (tag.contains("blueArmorDurability", Tag.TAG_INT)) {
                this.blueArmorDurability = Math.max(0, tag.getInt("blueArmorDurability"));
            }
        }
    }
}
