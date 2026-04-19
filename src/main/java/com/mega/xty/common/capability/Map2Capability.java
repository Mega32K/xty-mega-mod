package com.mega.xty.common.capability;

import com.mega.endinglib.api.capability.CapabilityEntityData;
import com.mega.endinglib.api.capability.CapabilitySyncType;
import com.mega.endinglib.api.capability.EntitySyncCapabilityBase;
import com.mega.endinglib.api.capability.syncher.CapabilityDataSerializers;
import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.common.data.EndingLibrarySavedData;
import com.mega.endinglib.common.data.InputOperations;
import com.mega.endinglib.proxy.CommonProxy;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.MapLevelEvents;
import com.mega.xty.common.data.map2.ClientGame1Data;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.data.map2.ServerGameData;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.proxy.ClientProxy;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class Map2Capability extends EntitySyncCapabilityBase {
    public static final ResourceLocation DEATH_PLAYER_SKIN = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/entity/death.png");
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "mega_map2");
    public final CapabilityEntityData<Integer> KILLCOUNT1 = this.dataManager.define(0, "killcount1", 0, CapabilityDataSerializers.INT);
    public final CapabilityEntityData<Integer> KILLCOUNT2 = this.dataManager.define(1, "killcount2", 0, CapabilityDataSerializers.INT);
    public final CapabilityEntityData<Boolean> NEED_START = this.dataManager.define(2, "needStart", false, CapabilityDataSerializers.BOOLEAN);
    /**
     * 隐身程度
     */
    public final CapabilityEntityData<Integer> SOUL_INVISIBLE = this.dataManager.define(3, "soulInvisible", 0, CapabilityDataSerializers.INT);
    public final CapabilityEntityData<Integer> CURRENT_EVOLUTION = this.dataManager.define(4, "evolution", 0, CapabilityDataSerializers.INT);
    public final CapabilityEntityData<Integer> EVOLUTION_KILL_COUNT = this.dataManager.define(5, "evolutionKillCount", 0, CapabilityDataSerializers.INT);
    public final CapabilityEntityData<Boolean> XAERO_DEAD = this.dataManager.define(6, "xaeroDead", false, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Optional<Vector3f>> LAST_DEATH = this.dataManager.define(7, "lastDeathPos", Optional.empty(), CapabilityDataSerializers.OPTIONAL_VEC3F);
    public final CapabilityEntityData<ItemStack> SLOT_0 = this.dataManager.defineWithoutSerialization(8, ItemStack.EMPTY, CapabilityDataSerializers.ITEM_STACK);
    public final CapabilityEntityData<ItemStack> SLOT_1 = this.dataManager.defineWithoutSerialization(9, ItemStack.EMPTY, CapabilityDataSerializers.ITEM_STACK);
    public final CapabilityEntityData<ItemStack> SLOT_2 = this.dataManager.defineWithoutSerialization(10, ItemStack.EMPTY, CapabilityDataSerializers.ITEM_STACK);
    public final CapabilityEntityData<ItemStack> SLOT_3 = this.dataManager.defineWithoutSerialization(11, ItemStack.EMPTY, CapabilityDataSerializers.ITEM_STACK);
    public final CapabilityEntityData<ItemStack> SLOT_4 = this.dataManager.defineWithoutSerialization(12, ItemStack.EMPTY, CapabilityDataSerializers.ITEM_STACK);
    public final CapabilityEntityData<ItemStack> SLOT_5 = this.dataManager.defineWithoutSerialization(13, ItemStack.EMPTY, CapabilityDataSerializers.ITEM_STACK);
    public Vec3 lastPos = new Vec3(0F, 0F, 0F);
    public int lastSoulInvisible;
    public NonNullList<ItemStack> clientEvolutionWeapons = Util.make(() -> {
        NonNullList<ItemStack> list = NonNullList.withSize(3, ItemStack.EMPTY);
        for (int i=0;i<3;i++)
            list.set(i, ItemStack.EMPTY);
        return list;
    });
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    protected @NotNull Predicate<Entity> canAttach() {
        return entity -> entity instanceof Player;
    }
    @Override
    public void onSyncedDataUpdated(CapabilityEntityData<?> data) {
        if (this.NEED_START.equals(data)) {
            if (this.getEntity() == ClientWrapped.clientPlayer()) {
                if (!this.isNeedToStart()) {
                    ClientProxy.closeMap2Screen();
                } else {
                    ClientProxy.openMap2StartScreen();
                }
            }
        } else if (this.LAST_DEATH.equals(data)) {
            if (this.getEntity() == ClientWrapped.clientPlayer()) {
                this.getLastDeathPos().ifPresent(pos-> ClientGameData.fpsSpectate());
            }
        } else if (this.XAERO_DEAD.equals(data)) {
            if (this.getEntity() == ClientWrapped.clientPlayer()) {
                if (!this.isXaeroDead()) {
                    Player player = ClientWrapped.clientPlayer();
                    player.setPos(player.position().add(0, -32F, 0F));
                    ClientWrapped.setCameraEntity(null);
                }
            }
        }
    }

    @Override
    public void syncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {
    }
    @Override
    public void readSyncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {

    }

    @Override
    public boolean canSyncWhenTick(Entity entity, Level level) {
        return false;
    }

    @Override
    public void customSerializeNBT(CompoundTag compoundTag) {
    }

    @Override
    public void customDeserializeNBT(CompoundTag compoundTag) {
        if (this.getEntity() instanceof ServerPlayer serverPlayer) {
            Map2SavedData data = Map2SavedData.getInstance(serverPlayer.server);
            if (!data.isStopped()) {
                this.updateXaeroDead(this.isXaeroDead(), serverPlayer, EndingLibrarySavedData.getInstance(serverPlayer.server));
            }
        }
    }

    @Override
    protected void tick(Entity entity) {
        if (entity instanceof Player player) {
            lastSoulInvisible = getSoulInvisible();
            if (!player.level().isClientSide) {
                if (player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get())) {
                    boolean onGround = player.onGround();
                    if (player.position().add(lastPos.scale(-1F)).horizontalDistance() > 0.1F) {
                        if (!player.isShiftKeyDown() && !player.hasPose(Pose.SWIMMING) && onGround) {
                            this.setSoulInvisible(this.getSoulInvisible() - 2);
                            this.setSoulInvisible(Mth.clamp(this.getSoulInvisible(), 0, 15));
                        }
                    }
                    if (getSoulInvisible() < 15 && (onGround || player.isShiftKeyDown() || player.hasPose(Pose.SWIMMING)))
                        this.setSoulInvisible(this.getSoulInvisible() + 1);
                }
                if (player.level() instanceof ServerLevel serverLevel) {
                    if (ServerGameData.map2Playing(serverLevel.getServer())) {
                        Inventory inventory = player.getInventory();
                        this.setSlot0(inventory.getItem(0).copy());
                        this.setSlot1(inventory.getItem(1).copy());
                        this.setSlot2(inventory.getItem(2).copy());
                        this.setSlot3(inventory.getItem(3).copy());
                        this.setSlot4(inventory.getItem(4).copy());
                        this.setSlot5(inventory.getItem(5).copy());
                    }
                }
            } else {
                if (!ClientGame1Data.isStopped && !ClientGameData.isStopped) {
                    if (player == ClientWrapped.clientPlayer()) {
                        this.clientEvolutionWeapons.clear();
                        for (int i = 0; i < 3; i++) {
                            int index = i + this.getEvolutionIndex();
                            if (index >= 0 && index < ClientGame1Data.evolutionWeapons.size()) {
                                this.clientEvolutionWeapons.set(i, ClientGame1Data.evolutionWeapons.get(index));
                            } else this.clientEvolutionWeapons.set(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
            lastPos = player.position();
        }
    }
    public float getInvisibleValue(float partialTicks) {
        int soulValue = this.getSoulInvisible();
        return Mth.clamp(1F - Mth.lerp(partialTicks, this.lastSoulInvisible, soulValue) / 15F, 0F, 0.5F);
    }
    public void set1KillCount(int time) {
        this.dataManager.setValue(KILLCOUNT1, time);
    }
    public int get1KillCount() {
        return this.dataManager.getValue(KILLCOUNT1);
    }
    public void set2KillCount(int time) {
        this.dataManager.setValue(KILLCOUNT2, time);
    }
    public int get2KillCount() {
        return this.dataManager.getValue(KILLCOUNT2);
    }
    public boolean isNeedToStart() {
        return this.dataManager.getValue(NEED_START);
    }
    public void setNeedStart(boolean v) {
        this.dataManager.setValue(NEED_START, v);
    }
    public void setSoulInvisible(int value) {
        this.dataManager.setValue(SOUL_INVISIBLE, value);
    }
    public int getSoulInvisible() {
        return this.dataManager.getValue(SOUL_INVISIBLE);
    }
    public void setEvolutionIndex(int value) {
        this.dataManager.setValue(CURRENT_EVOLUTION, value);
    }
    public int getEvolutionIndex() {
        return this.dataManager.getValue(CURRENT_EVOLUTION);
    }
    public void setEvolutionKillCount(int value) {
        this.dataManager.setValue(EVOLUTION_KILL_COUNT, value);
    }
    public int getEvolutionKillCount() {
        return this.dataManager.getValue(EVOLUTION_KILL_COUNT);
    }
    public void setXaeroDead(boolean value) {
        this.dataManager.setValue(XAERO_DEAD, value);
        if (this.XAERO_DEAD.isDirty()) {
            if (this.getEntity() instanceof ServerPlayer player) {
                EndingLibrarySavedData elSavedData = EndingLibrarySavedData.readOrCreate(player.server);
                Map2SavedData map2SavedData = Map2SavedData.getInstance(player.server);
                Inventory inventory = player.getInventory();
                if (value) {
                    map2SavedData.storeDeadPlayerInventory(player);
                    inventory.clearContent();
                } else {
                    var inv = map2SavedData.pickUpDeadSavedInv(player);
                    if (inv != null) inv.overridePlayerInv(player);
                }
                updateXaeroDead(value, player, elSavedData);
            }
        }
    }

    public void updateXaeroDead(boolean value, ServerPlayer player, EndingLibrarySavedData savedData) {
        if (value) {CommonProxy.getCameraCapOptional(player).ifPresent(cap -> {
            setLastDeathPos(player.position().toVector3f().add(0, 32, 0));
            cap.setCustomSkin(DEATH_PLAYER_SKIN.toString());
            savedData.addDisabledPermission(player, InputOperations.MOVEMENT);
            savedData.addDisabledPermission(player, InputOperations.SNEAK);

            savedData.addDisabledOverlay(player, VanillaGuiOverlay.ARMOR_LEVEL.id());
            savedData.addDisabledOverlay(player, VanillaGuiOverlay.PLAYER_HEALTH.id());
            savedData.addDisabledOverlay(player, VanillaGuiOverlay.FOOD_LEVEL.id());
            savedData.addDisabledOverlay(player, VanillaGuiOverlay.HOTBAR.id());
            savedData.addDisabledOverlay(player, VanillaGuiOverlay.EXPERIENCE_BAR.id());
            player.serverLevel().levelEvent(player, 110120, BlockPos.ZERO, MapLevelEvents.FPS_SPECTATE);
        });
            CommonProxy.getEntityCapOptional(player).ifPresent(cap -> {
                cap.setRenderScale(new Vector3f(0F, 0F, 0F));
                cap.setRenderScaleInterpolationDuration(1);
            });
        } else {
            CommonProxy.getCameraCapOptional(player).ifPresent(cap -> {
                this.getLastDeathPos().ifPresent(pos -> {
                    setLastDeathPos(null);
                });
                player.fallDistance = 0F;
                cap.setCustomSkin("");
                savedData.removeDisabledPermission(player, InputOperations.MOVEMENT);
                savedData.removeDisabledPermission(player, InputOperations.MOVE_LEFT);
                savedData.removeDisabledPermission(player, InputOperations.MOVE_RIGHT);
                savedData.removeDisabledPermission(player, InputOperations.MOVE_FORWARD);
                savedData.removeDisabledPermission(player, InputOperations.MOVE_BACKWARD);
                savedData.removeDisabledPermission(player, InputOperations.SNEAK);

                savedData.removeDisabledOverlay(player, VanillaGuiOverlay.ARMOR_LEVEL.id());
                savedData.removeDisabledOverlay(player, VanillaGuiOverlay.PLAYER_HEALTH.id());
                savedData.removeDisabledOverlay(player, VanillaGuiOverlay.FOOD_LEVEL.id());
                savedData.removeDisabledOverlay(player, VanillaGuiOverlay.HOTBAR.id());
                savedData.removeDisabledOverlay(player, VanillaGuiOverlay.EXPERIENCE_BAR.id());
            });
            CommonProxy.getEntityCapOptional(player).ifPresent(cap -> {
                cap.setRenderScale(new Vector3f(1F, 1F,1F));
                cap.setRenderScaleEasing(Easing.IN_OUT_CUBIC);
                cap.setRenderScaleInterpolationDuration(15);
            });
        }
    }
    public boolean isXaeroDead() {
        return this.dataManager.getValue(XAERO_DEAD);
    }
    public void setLastDeathPos(@Nullable Vector3f value) {
        this.dataManager.setValue(LAST_DEATH, Optional.ofNullable(value));
    }
    public Optional<Vector3f> getLastDeathPos() {
        return this.dataManager.getValue(LAST_DEATH);
    }
    public void setSlot0(ItemStack itemStack) {
        if (!itemEquals(itemStack, this.getSlot0())) {
            this.dataManager.setValue(SLOT_0, itemStack, true);
        }
    }
    public ItemStack getSlot0() {
        return this.dataManager.getValue(SLOT_0);
    }
    public void setSlot1(ItemStack itemStack) {
        if (!itemEquals(itemStack, this.getSlot1())) {
            this.dataManager.setValue(SLOT_1, itemStack, true);
        }
    }
    public ItemStack getSlot1() {
        return this.dataManager.getValue(SLOT_1);
    }
    public void setSlot2(ItemStack itemStack) {
        if (!itemEquals(itemStack, this.getSlot2())) {
            this.dataManager.setValue(SLOT_2, itemStack, true);
        }
    }
    public ItemStack getSlot2() {
        return this.dataManager.getValue(SLOT_2);
    }
    public void setSlot3(ItemStack itemStack) {
        if (!itemEquals(itemStack, this.getSlot3())) {
            this.dataManager.setValue(SLOT_3, itemStack, true);
        }
    }
    public ItemStack getSlot3() {
        return this.dataManager.getValue(SLOT_3);
    }
    public void setSlot4(ItemStack itemStack) {
        if (!itemEquals(itemStack, this.getSlot4())) {
            this.dataManager.setValue(SLOT_4, itemStack, true);
        }
    }
    public ItemStack getSlot4() {
        return this.dataManager.getValue(SLOT_4);
    }
    public void setSlot5(ItemStack itemStack) {
        if (!itemEquals(itemStack, this.getSlot5())) {
            this.dataManager.setValue(SLOT_5, itemStack, true);
        }
    }
    public ItemStack getSlot5() {
        return this.dataManager.getValue(SLOT_5);
    }
    public ItemStack getSlot(int i) {
        switch (i) {
            case 0 -> {
                return getSlot0();
            }
            case 1 -> {
                return getSlot1();
            }
            case 2 -> {
                return getSlot2();
            }
            case 3 -> {
                return getSlot3();
            }
            case 4 -> {
                return getSlot4();
            }
            case 5 -> {
                return getSlot5();
            }
        }
        return ItemStack.EMPTY;
    }
    public static boolean itemEquals(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1.is(itemStack2.getItem())) {
            if (itemStack1.isEmpty() && itemStack2.isEmpty()) {
                return true;
            } else if (itemStack1.isEmpty() || itemStack2.isEmpty())
                return false;
            else {
                return itemStack1.getCount() == itemStack2.getCount() && Objects.equals(itemStack1.getTag(), itemStack2.getTag()) && itemStack1.areCapsCompatible(itemStack2);
            }
        }
        return false;
    }
}
