package com.mega.xty.common.capability;

import com.mega.endinglib.EndingLibrary;
import com.mega.endinglib.api.capability.CapabilityEntityData;
import com.mega.endinglib.api.capability.CapabilitySyncType;
import com.mega.endinglib.api.capability.EntitySyncCapabilityBase;
import com.mega.endinglib.api.capability.syncher.CapabilityDataSerializers;
import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.common.data.EndingLibrarySavedData;
import com.mega.endinglib.common.data.InputOperations;
import com.mega.endinglib.proxy.CommonProxy;
import com.mega.endinglib.util.mixin.data_expand.ExtraEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class XtyModPlayerCapability extends EntitySyncCapabilityBase {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(EndingLibrary.MODID, "xty_player");
    public final CapabilityEntityData<Byte> DISABLED_LIMBS = this.dataManager.define(0, "disabledLimbs", (byte)0, CapabilityDataSerializers.BYTE);
    public final CapabilityEntityData<Boolean> GAME_INVULNERABLE = this.dataManager.define(1, "gameInvulnerable", false, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> GAME_INVULNERABLE2 = this.dataManager.define(2, "gameInvulnerable2", false, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Float> HEALTH = this.dataManager.defineWithoutSerialization(3, 0F, CapabilityDataSerializers.FLOAT);
    public final CapabilityEntityData<Float> MAX_HEALTH = this.dataManager.defineWithoutSerialization(4, 0F, CapabilityDataSerializers.FLOAT);
    public final CapabilityEntityData<Integer> NO_PHYSICS_TIME = this.dataManager.defineWithoutSerialization(5, -1, CapabilityDataSerializers.INT);
    public Easing smoothTeleportEasing = Easing.LINEAR;
    public Vec3 smoothTeleportTarget;
    public Vec3 smoothStartPos;
    public int smoothTeleportDuration;
    public int smoothTeleportStart;
    public boolean canUsePartialTeleportAnim;
    public boolean xlCollision = false;
    public int verticalGame2MultiJumpTime = 0;
    public float calculateInterpolationProgress(float partialTicks) {
        int i = this.smoothTeleportDuration;
        if (i <= 0 || this.getEntity() == null) {
            return 1.0F;
        } else {
            float f =  ExtraEntity.of(this.getEntity()).endinglib$getExtraEntityData().tickCount - this.smoothTeleportStart;
            float f1 = f + partialTicks;
            return this.smoothTeleportEasing.calculate(Mth.clamp(Mth.inverseLerp(f1, 0.0F, (float)i), 0.0F, 1.0F));
        }
    }
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
        super.onSyncedDataUpdated(data);
        if (data.equals(NO_PHYSICS_TIME)) {
            if (this.getEntity() != null) {
                if (this.getNoPhysicsTime() > 0) {
                    this.getEntity().noPhysics = true;
                } else if (this.getNoPhysicsTime() == 0)
                    this.getEntity().noPhysics = false;
            }
        }
    }

    @Override
    public void syncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {
        if (entity instanceof Player player) {
            if (capabilitySyncType == CapabilitySyncType.PLAYER_RESPAWN || capabilitySyncType == CapabilitySyncType.PLAYER_CLONE) {
                if (this.dataManager.getValue(DISABLED_LIMBS) > 0) {
                    CommonProxy.getEntityCapOptional(player).ifPresent(capability -> {
                        capability.setCustomHitbox(Optional.empty());
                    });

                    if (entity.level() instanceof ServerLevel serverLevel) {
                        EndingLibrarySavedData data = EndingLibrarySavedData.readOrCreate(serverLevel.getServer());
                        data.removeDisabledPermission(player, InputOperations.MOUSE_ATTACK);
                        data.removeDisabledPermission(player, InputOperations.MOUSE_USE);
                    }
                }
                this.dataManager.setValue(DISABLED_LIMBS, DISABLED_LIMBS.getInitValue());
            }
        }
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
    }

    @Override
    protected void tick(Entity entity) {
        if (entity instanceof Player player) {
            if (this.getNoPhysicsTime() >= 0) {
                player.noPhysics = true;
            }
            if (!player.level().isClientSide) {
                if (this.getNoPhysicsTime() >= 0) {
                    this.setNoPhysicsTime(this.getNoPhysicsTime() - 1);
                }
            }
            if (player.isAlive()) {
                if (verticalGame2MultiJumpTime > 0)
                    verticalGame2MultiJumpTime--;
                if (this.isLimbDisabled(Limbs.LEFT_LEG) || this.isLimbDisabled(Limbs.RIGHT_LEG))
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, true));

                if (this.isLimbDisabled(Limbs.BODY)) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 6, false, false, true));
                    Pose pose = player.getPose();
                    EntityDimensions dimensions = player.getDimensions(pose);
                    if (pose != Pose.SLEEPING && pose != Pose.SWIMMING && pose != Pose.SPIN_ATTACK && pose != Pose.DYING && pose != Pose.SITTING)
                        CommonProxy.getEntityCapOptional(player).ifPresent(capability -> capability.setCustomHitbox(new AABB(dimensions.width * -0.45, dimensions.height * 0.75, dimensions.width * -0.45, dimensions.width * 0.45, dimensions.height, dimensions.width * 0.45)));
                } else {
                    if (this.isLimbDisabled(Limbs.LEFT_LEG) && this.isLimbDisabled(Limbs.RIGHT_LEG)) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 5, false, false, true));
                        Pose pose = player.getPose();
                        EntityDimensions dimensions = player.getDimensions(pose);
                        if (pose != Pose.SLEEPING && pose != Pose.SWIMMING && pose != Pose.SPIN_ATTACK && pose != Pose.DYING && pose != Pose.SITTING)
                            CommonProxy.getEntityCapOptional(player).ifPresent(capability -> capability.setCustomHitbox(new AABB(dimensions.width * -0.5, dimensions.height * 0.375, dimensions.width * -0.5, dimensions.width * 0.5, dimensions.height, dimensions.width * 0.5)));
                    }
                }
            } else {
                if (this.isLimbDisabled(Limbs.BODY) || this.isLimbDisabled(Limbs.LEFT_LEG) || this.isLimbDisabled(Limbs.RIGHT_LEG)) {
                    CommonProxy.getEntityCapOptional(player).ifPresent(capability -> capability.setCustomHitbox(Optional.empty()));
                }
            }
        }
    }
    public boolean isGameInvul() {
        return this.dataManager.getValue(GAME_INVULNERABLE);
    }
    public void setGameInvulnerable(boolean flag) {
        this.dataManager.setValue(GAME_INVULNERABLE, flag);
    }
    public boolean isGameInvul2() {
        return this.dataManager.getValue(GAME_INVULNERABLE2);
    }
    public void setGameInvulnerable2(boolean flag) {
        this.dataManager.setValue(GAME_INVULNERABLE2, flag);
    }
    public boolean isLimbDisabled(Limbs limbs) {
        return CompoundTagUtils.getByteFlag(this.dataManager.getValue(DISABLED_LIMBS), limbs.getFlag());
    }
    public void setLimbDisabled(Limbs limbs, boolean disabled) {
        CompoundTagUtils.setByteFlags((b)-> this.dataManager.setValue(DISABLED_LIMBS, b), this.dataManager.getValue(DISABLED_LIMBS), limbs.getFlag(), disabled);
    }
    public float getGame2Health() {
        return this.dataManager.getValue(HEALTH);
    }
    public float getGame2MaxHealth() {
        return this.dataManager.getValue(MAX_HEALTH);
    }
    public void setGame2Health(float health) {
        this.dataManager.setValue(HEALTH, health);
    }
    public void setGame2MaxHealth(float health) {
        this.dataManager.setValue(MAX_HEALTH, health);
    }
    public void setNoPhysicsTime(int time) {
        this.dataManager.setValue(NO_PHYSICS_TIME, time);
    }
    public int getNoPhysicsTime() {
        return this.dataManager.getValue(NO_PHYSICS_TIME);
    }
}
