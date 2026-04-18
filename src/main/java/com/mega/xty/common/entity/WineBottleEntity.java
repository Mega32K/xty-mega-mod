package com.mega.xty.common.entity;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.mixin.accessor.AccessorCommandSourceStack;
import com.mega.xty.common.item.armor.WineBottleHat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

public class WineBottleEntity extends Interaction implements OwnableEntity, Attackable {
    @Nullable
    private Player owner;
    private String ownerName;
    @javax.annotation.Nullable
    private LivingEntity lastHurtByMob;
    private int lastHurtByMobTimestamp;

    final Quaternionf rotation = new Quaternionf(0, 0, 0, 1);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(WineBottleEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<String> DATA_COMMAND = SynchedEntityData.defineId(WineBottleEntity.class, EntityDataSerializers.STRING);

    public WineBottleEntity(EntityType<?> entityType, Level level) {
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
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity p_273553_) {
        return false;
    }

    @Override
    public InteractionResult interact(Player p_273507_, InteractionHand p_273048_) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float damage) {
        Entity entity1 = damageSource.getEntity();
        if (entity1 != null) {
            if (entity1 instanceof LivingEntity livingentity1) {
                if (!damageSource.is(DamageTypeTags.NO_ANGER)) {
                    this.setLastHurtByMob(livingentity1);
                }
            }
        }
        if (damageSource.getEntity() instanceof Player) {
            if (this.getOwner() instanceof Player ownerP && !ownerP.isSpectator()) {
                if (damageSource.getDirectEntity() instanceof Projectile projectile) {
                    String c = this.getFlagCommand();
                    if (!c.isEmpty()) {
                        if (this.level() instanceof ServerLevel sl) {
                            CommandSourceStack css = createCommandSourceStack().withSuppressedOutput();
                            sl.getServer().getCommands().performPrefixedCommand(css.withMaximumPermission(2), c);
                        }
                    }
                    if (projectile instanceof AbstractArrow arrow) {
                        arrow.discard();
                    }
                    return true;
                }
            }
        }
        return super.hurt(damageSource, damage);
    }

    public void setLastHurtByMob(LivingEntity lastHurtByMob) {
        this.lastHurtByMob = lastHurtByMob;
    }

    @Nullable
    @Override
    public LivingEntity getLastAttacker() {
        return this.lastHurtByMob;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.lastHurtByMob != null) {
            if (!this.lastHurtByMob.isAlive()) {
                this.setLastHurtByMob((LivingEntity)null);
            } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                this.setLastHurtByMob((LivingEntity)null);
            }
        }
        if (this.getOwner() instanceof Player player) {
            owner = player;
            ownerName = owner.getGameProfile().getName();
            double[] offset = getOffset();
            this.setPos(player.position().add(0,  offset[1], 0));
            xOld = owner.xOld + offset[0];
            yOld = owner.yOld + offset[1];
            zOld = owner.zOld + offset[2];
            if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof WineBottleHat)) {
                if (!level().isClientSide) {
                    player.removeTag(WineBottleHat.TAG);
                    this.discard();
                }
            }
        } else if (!level().isClientSide) {
            discard();
        }
    }
    public double[] getOffset() {
        if (owner != null) {
            Vector3f up0 = new Vector3f(0.0F, 1.0F, 0.0F).rotate(this.rotation).mul(0.45F);
            return new double[] {
                    up0.x ,
                    up0.y + (!owner.isShiftKeyDown() ? 1.35F : 1F),
                    up0.z };
        }
        return new double[] {0D, 0D, 0D};
    }
    @Override
    protected AABB makeBoundingBox() {
        if (owner != null && !owner.isSpectator()) {
            rotation.set(0,0,0,1).rotateYXZ(-owner.getViewYRot(0.5F) * Mth.DEG_TO_RAD, owner.getViewXRot(0.5F) * Mth.DEG_TO_RAD, 0.0F);
            AABB aabb = new AABB(-0.13, -0.3, -0.16, 0.13, 0.7F, 0.16);
            Collection<Vec3> points = transformPoints(pointsAABB(aabb));
            Vec3 pos = position();
            double[] x = limitX(points);
            double[] y = limitY(points);
            double[] z = limitZ(points);
            aabb = new AABB(new Vec3(x[0], y[0], z[0]).add(pos), new Vec3(x[1], y[1], z[1]).add(pos));

            return aabb;
        }
        return super.makeBoundingBox();
    }
    public Collection<Vec3> pointsAABB(AABB aabb) {
        List<Vec3> list = new ObjectArrayList<>(8);
        list.add(new Vec3(aabb.minX, aabb.minY, aabb.minZ));
        list.add(new Vec3(aabb.minX, aabb.minY, aabb.maxZ));
        list.add(new Vec3(aabb.maxX, aabb.minY, aabb.minZ));
        list.add(new Vec3(aabb.maxX, aabb.minY, aabb.maxZ));
        list.add(new Vec3(aabb.minX, aabb.maxY, aabb.minZ));
        list.add(new Vec3(aabb.minX, aabb.maxY, aabb.maxZ));
        list.add(new Vec3(aabb.maxX, aabb.maxY, aabb.minZ));
        list.add(new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ));
        return list;
    }
    public Collection<Vec3> transformPoints(Collection<Vec3> points) {
        List<Vec3> list = new ObjectArrayList<>(points.size());
        Vector3f up0 = new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f left0 = new Vector3f(1.0F, 0.0F, 0.0F);
        Vector3f forwards0 = new Vector3f(0.0F, 0.0F, 1.0F);
        for (Vec3 point : points) {
            up0 = up0.set(0.0F, 1.0F, 0.0F).rotate(this.rotation).mul((float) point.y);
            left0 = left0.set(1.0F, 0.0F, 0.0F).rotate(this.rotation).mul((float) point.x);
            forwards0 = forwards0.set(0.0F, 0.0F, 1.0F).rotate(this.rotation).mul((float) point.z);
            list.add(new Vec3(left0.x + forwards0.x + up0.x, left0.y + forwards0.y + up0.y, left0.z + forwards0.z + up0.z));
        }
        return list;
    }
    double[] limitX(Collection<Vec3> vec3s) {
        double min = Double.NaN;
        double max = Double.NaN;
        for (Vec3 vec3 : vec3s) {
            if (Double.isNaN(min) || Double.isNaN(max)) {
                min = max = vec3.x;
            } else {
                if (min >= vec3.x) {
                    min = vec3.x;
                }
                if (max <= vec3.x) {
                    max = vec3.x;
                }
            }
        }
        return new double[] {min, max};
    }
    double[] limitY(Collection<Vec3> vec3s) {
        double min = Double.NaN;
        double max = Double.NaN;
        for (Vec3 vec3 : vec3s) {
            if (Double.isNaN(min) || Double.isNaN(max)) {
                min = max = vec3.y;
            } else {
                if (min >= vec3.y) {
                    min = vec3.y;
                }
                if (max <= vec3.y) {
                    max = vec3.y;
                }
            }
        }
        return new double[] {min, max};
    }
    double[] limitZ(Collection<Vec3> vec3s) {
        double min = Double.NaN;
        double max = Double.NaN;
        for (Vec3 vec3 : vec3s) {
            if (Double.isNaN(min) || Double.isNaN(max)) {
                min = max = vec3.z;
            } else {
                if (min >= vec3.z) {
                    min = vec3.z;
                }
                if (max <= vec3.z) {
                    max = vec3.z;
                }
            }
        }
        return new double[] {min, max};
    }
    @Override
    public double getX(double partialTicks) {
        if (owner != null) {
            return owner.getX(partialTicks) + getOffset()[0];
        }
        return super.getX(partialTicks);
    }

    @Override
    public double getY(double partialTicks) {
        if (owner != null) {
            return owner.getY(partialTicks) + getOffset()[1];
        }
        return super.getY(partialTicks);
    }

    @Override
    public double getZ(double partialTicks) {
        if (owner != null) {
            return owner.getZ(partialTicks) + getOffset()[2];
        }
        return super.getZ(partialTicks);
    }

    @Override
    public double getX() {
        if (owner != null) {
            return owner.getX() + getOffset()[0];
        }
        return super.getX();
    }

    @Override
    public double getY() {
        if (owner != null) {
            return owner.getY()+  getOffset()[1];
        }
        return super.getY();
    }

    @Override
    public double getZ() {
        if (owner != null) {
            return owner.getZ() + getOffset()[2];
        }
        return super.getZ();
    }

    @Override
    public @NotNull Vec3 position() {
        if (owner != null) {
            double[] offset = getOffset();
            return owner.position().add(offset[0],  offset[1], offset[2]);
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
        tag.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
        if (ownerName != null && !ownerName.isEmpty())
            tag.putString("OwnerName", ownerName);
        if (this.getOwnerUUID() != null) {
            tag.putUUID("Owner", this.getOwnerUUID());
        }
        if (!this.getFlagCommand().isEmpty())
            tag.putString("FlagCommand", this.getFlagCommand());
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lastHurtByMobTimestamp = tag.getInt("HurtByTimestamp");
        UUID uuid;
        if (CompoundTagUtils.containsString(tag, "OwnerName"))
            ownerName = tag.getString("FlagCommand");
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

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> eda) {
        super.onSyncedDataUpdated(eda);
        if (DATA_OWNERUUID_ID.equals(eda)) {

        }
    }
}
