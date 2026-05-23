package com.mega.map.common.entity;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.map.common.data.map1.Game2SavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThrownItemEntity extends AbstractArrow {
    private static final EntityDataAccessor<Integer> BINDING_ITEM = SynchedEntityData.defineId(ThrownItemEntity.class, EntityDataSerializers.INT);
    public ThrownItemEntity(EntityType<ThrownItemEntity> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }
    public void setBindingItem(ItemEntity item) {
        if (item == null || item.isRemoved())
            this.entityData.set(BINDING_ITEM, -1);
        else this.entityData.set(BINDING_ITEM, item.getId());
    }
    @Nullable
    public ItemEntity getBindingItem() {
        int id = this.entityData.get(BINDING_ITEM);
        if (id < 0) return null;
        Entity entity = this.level().getEntity(id);
        if (entity instanceof ItemEntity item && item.isAlive())
            return item;
        return null;
    }
    @Override
    public void tick() {
        super.tick();
        ItemEntity entity = this.getBindingItem();
        if (entity != null) {
            this.setDeltaMovement(entity.getDeltaMovement());
            this.setPos(entity.position());
        } else if (!this.level().isClientSide)
            discard();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult hitResult) {
        if (this.getOwner() instanceof ServerPlayer player && hitResult.getEntity() != player
                && hitResult.getEntity() != this.getBindingItem()
                && hitResult.getEntity() instanceof LivingEntity) {
            Game2SavedData savedData = Game2SavedData.getInstance(player.server);
            if (!savedData.isStopped()) {
                hitResult.getEntity().hurt(this.level().damageSources().genericKill(), 50);
                this.discard();
            }
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void playSound(@NotNull SoundEvent p_19938_, float p_19939_, float p_19940_) {
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BINDING_ITEM, -1);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (CompoundTagUtils.containsInt(tag, "BindingItem"))
            this.entityData.set(BINDING_ITEM, tag.getInt("BindingItem"));
        CompoundTag compoundtag = tag.getCompound("Item");
    }
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BindingItem", this.entityData.get(BINDING_ITEM));
    }
}
