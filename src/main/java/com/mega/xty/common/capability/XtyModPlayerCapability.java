package com.mega.xty.common.capability;

import com.mega.endinglib.EndingLibrary;
import com.mega.endinglib.api.capability.CapabilityEntityData;
import com.mega.endinglib.api.capability.CapabilitySyncType;
import com.mega.endinglib.api.capability.ELCapabilityManager;
import com.mega.endinglib.api.capability.EntitySyncCapabilityBase;
import com.mega.endinglib.api.capability.syncher.CapabilityDataSerializers;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.proxy.CommonProxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class XtyModPlayerCapability extends EntitySyncCapabilityBase {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(EndingLibrary.MODID, "xty_player");
    public final CapabilityEntityData<Byte> DISABLED_LIMBS = this.dataManager.define(0, "disabledLimbs", (byte)0, CapabilityDataSerializers.BYTE);
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    protected @NotNull Predicate<Entity> canAttach() {
        return entity -> entity instanceof Player;
    }

    @Override
    public void syncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {
        if (dist == Dist.DEDICATED_SERVER) {
            if (capabilitySyncType == CapabilitySyncType.PLAYER_RESPAWN || capabilitySyncType == CapabilitySyncType.PLAYER_CLONE) {
                if (this.isLimbDisabled(Limbs.RIGHT_LEG) && this.isLimbDisabled(Limbs.LEFT_LEG)) {
                    if (entity instanceof Player player)
                        CommonProxy.getEntityCapOptional(player).ifPresent(capability -> capability.setCustomHitbox(Optional.empty()));
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
        if (entity instanceof Player player && player.isAlive()) {
            if (this.isLimbDisabled(Limbs.LEFT_LEG) && this.isLimbDisabled(Limbs.RIGHT_LEG)) {
                Pose pose = player.getPose();
                EntityDimensions dimensions = player.getDimensions(pose);
                if (pose != Pose.SLEEPING && pose != Pose.SWIMMING && pose != Pose.SPIN_ATTACK && pose != Pose.DYING && pose != Pose.SITTING)
                    CommonProxy.getEntityCapOptional(player).ifPresent(capability -> capability.setCustomHitbox(new AABB(dimensions.width* -0.5, dimensions.height*0.375, dimensions.width * -0.5, dimensions.width* 0.5, dimensions.height, dimensions.width*0.5)));
            }
        }
    }

    public boolean isLimbDisabled(Limbs limbs) {
        return CompoundTagUtils.getByteFlag(this.dataManager.getValue(DISABLED_LIMBS), limbs.getFlag());
    }
    public void setLimbDisabled(Limbs limbs, boolean disabled) {
        CompoundTagUtils.setByteFlags((b)-> this.dataManager.setValue(DISABLED_LIMBS, b), this.dataManager.getValue(DISABLED_LIMBS), limbs.getFlag(), disabled);
    }
}
