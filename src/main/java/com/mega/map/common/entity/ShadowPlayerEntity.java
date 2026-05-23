package com.mega.map.common.entity;

import com.mega.map.common.init.EntityInit;
import com.mega.map.proxy.ClientProxy;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ShadowPlayerEntity extends Mob {
    public static final EntityDataAccessor<Integer> OWNER_PLAYER = SynchedEntityData.defineId(ShadowPlayerEntity.class, EntityDataSerializers.INT);
    public int frozenTick;
    @Nullable
    public Object renderer = null;
    public double sXOld;
    public double sYOld;
    public double sZOld;
    public double sX;
    public double sY;
    public double sZ;
    public float sYBodyRot;
    public float sYBodyRotOld;
    public float sYHeadRot;
    public float sYHeadRotOld;
    public float sXRot;
    public float sXRotOld;
    public float sWalkSpeed;
    public float sWalkPosition;
    public float sAttackAnim;
    public ItemStack sMainHandItem = ItemStack.EMPTY;
    public ItemStack sOffHandItem = ItemStack.EMPTY;
    public Pose sPose = Pose.STANDING;
    public float sSwimAmount = 0F;
    public ShadowPlayerEntity(EntityType<? extends Mob> p_21368_, Level level) {
        super(p_21368_, level);
        frozenTick = 0;
        this.noPhysics = true;
    }

    public ShadowPlayerEntity(Player player) {
        super(EntityInit.SHADOW_PLAYER.get(), player.level());
        this.noPhysics = true;
        frozenTick = player.tickCount;
        this.setBind(player);
        this.sXOld = player.xOld;
        this.sYOld = player.yOld;
        this.sZOld = player.zOld;
        this.sX = player.getX();
        this.sY = player.getY();
        this.sZ = player.getZ();
        this.sPose = player.getPose();
        this.sXRot = player.getXRot();
        this.sXRotOld = player.xRotO;
        this.sYBodyRotOld = player.yBodyRotO;
        this.sYBodyRot = player.yBodyRot;
        this.sYHeadRotOld = player.yHeadRotO;
        this.sYHeadRot = player.yHeadRot;
        this.sSwimAmount = player.getSwimAmount(0F);
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public float getHealth() {
        return this.getMaxHealth();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_PLAYER, -1);
    }
    public @Nullable Player getBind() {
        int id = this.entityData.get(OWNER_PLAYER);
        if (id < 0) return null;
        Entity entity = this.level().getEntity(id);
        if (entity instanceof Player player)
            return player;
        else return null;
    }
    public void setBind(Player player) {
        this.entityData.set(OWNER_PLAYER, player.getId());
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        this.setPos(this.sX, this.sY, this.sZ);
        if (level().isClientSide) {
            this.xOld = this.sX;
            this.yOld = this.sY;
            this.zOld = this.sZ;
        }
        super.tick();
        if (this.tickCount >= 14) {
            if (!this.level().isClientSide) this.discard();
        }
        if (this.getBind() == null) {
            if (level().isClientSide) {
                this.setInvisible(true);
            } else this.discard();
        }
    }

    @Override
    public void kill() {
        this.discard();
    }

    @Override
    public void playSound(SoundEvent p_19938_, float p_19939_, float p_19940_) {
    }

    @Override
    public Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public void move(MoverType p_19973_, Vec3 p_19974_) {
    }

    @Override
    public void travel(Vec3 p_21280_) {
    }

    @Override
    public Pose getPose() {
        return sPose;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> eda) {
        super.onSyncedDataUpdated(eda);
        if (eda.equals(OWNER_PLAYER)) {
            Player player;
            if ((player = getBind()) != null) {
                this.setInvisible(false);
                frozenTick = player.tickCount;
                this.setBind(player);
                this.sXOld = player.xOld;
                this.sYOld = player.yOld;
                this.sZOld = player.zOld;
                this.sX = player.getX();
                this.sY = player.getY();
                this.sZ = player.getZ();
                this.sPose = player.getPose();
                this.sXRot = player.getXRot();
                this.sXRotOld = player.xRotO;
                this.sYBodyRotOld = player.yBodyRotO;
                this.sYBodyRot = player.yBodyRot;
                this.sYHeadRotOld = player.yHeadRotO;
                this.sYHeadRot = player.yHeadRot;
                this.sSwimAmount = player.getSwimAmount(0F);
                if (player.level().isClientSide) {
                    ClientProxy.setObj(this, player);
                }
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity p_20303_) {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public float getSwimAmount(float p_20999_) {
        return sSwimAmount;
    }
}
