package com.mega.xty.common.entity;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.mixin.accessor.AccessorEntity;
import com.mega.xty.common.item.armor.WineBottleHat;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BindingEntity extends Interaction implements OwnableEntity {
    @Nullable
    private Player owner;
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(BindingEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<String> DATA_COMMAND = SynchedEntityData.defineId(BindingEntity.class, EntityDataSerializers.STRING);

    public BindingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    public boolean isOwnedBy(LivingEntity p_21831_) {
        return p_21831_ == this.getOwner();
    }
    public String getFlagCommand() {
        return this.entityData.get(DATA_COMMAND);
    }
    public void setFlagCommand(String command) {
        this.entityData.set(DATA_COMMAND, command);
    }
    @javax.annotation.Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@javax.annotation.Nullable UUID p_21817_) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(p_21817_));
    }
    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float damage) {
        if (damageSource.getEntity() instanceof Player) {
            if (damageSource.getDirectEntity() instanceof Projectile) {
                String c = this.getFlagCommand();
                if (!c.isEmpty()) {
                    if (this.level() instanceof ServerLevel sl) {
                        sl.getServer().getCommands().performPrefixedCommand(this.createCommandSourceStack().withMaximumPermission(2), c);
                    }
                }
            }
        }
        return super.hurt(damageSource, damage);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getOwner() instanceof Player player) {
            owner = player;
            EntityDimensions dimensions = ((AccessorEntity) player).getDimensions();
            this.setPos(player.position().add(0, dimensions.height, 0));
            xOld = owner.xOld;
            yOld = owner.yOld + dimensions.height;
            zOld = owner.zOld;
            if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof WineBottleHat)) {
                if (!level().isClientSide) {
                    player.removeTag(WineBottleHat.TAG);
                    this.discard();
                }
            }
        }
    }

    @Override
    public double getX(double partialTicks) {
        if (owner != null) {
            return owner.getX(partialTicks);
        }
        return super.getX(partialTicks);
    }

    @Override
    public double getY(double partialTicks) {
        if (owner != null) {
            return owner.getY(partialTicks)+((AccessorEntity) owner).getDimensions().height;
        }
        return super.getY(partialTicks);
    }

    @Override
    public double getZ(double partialTicks) {
        if (owner != null) {
            return owner.getZ(partialTicks);
        }
        return super.getZ(partialTicks);
    }

    @Override
    public double getX() {
        if (owner != null) {
            return owner.getX();
        }
        return super.getX();
    }

    @Override
    public double getY() {
        if (owner != null) {
            return owner.getY()+((AccessorEntity) owner).getDimensions().height;
        }
        return super.getY();
    }

    @Override
    public double getZ() {
        if (owner != null) {
            return owner.getZ();
        }
        return super.getZ();
    }

    @Override
    public @NotNull Vec3 position() {
        if (owner != null) {
            EntityDimensions dimensions = ((AccessorEntity) owner).getDimensions();
            return owner.position().add(0, dimensions.height,0);
        }
        return super.position();
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return OwnableEntity.super.getOwner();
    }


    protected void defineSynchedData() {
        super.defineSynchedData();
        //this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        this.entityData.define(DATA_COMMAND, "");
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) {
            tag.putUUID("Owner", this.getOwnerUUID());
        }
        if (!this.getFlagCommand().isEmpty())
            tag.putString("FlagCommand", this.getFlagCommand());
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        UUID uuid;
        if (CompoundTagUtils.containsString(tag, "FlagCommand"))
            this.setFlagCommand(tag.getString("FlagCommand"));
        if (tag.hasUUID("Owner")) {
            uuid = tag.getUUID("Owner");
        } else {
            String s = tag.getString("Owner");
            //noinspection DataFlowIssue
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
            } catch (Throwable ignore)  {
            }
        }
    }
}
