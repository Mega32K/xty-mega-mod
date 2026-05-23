package com.mega.map.common.entity;

import com.mega.endinglib.api.client.Easing;
import com.mega.map.common.data.map1.ClientGame2Data;
import com.mega.map.common.data.map1.Game2SavedData;
import com.mega.map.game2.ShakeHandler;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BlackHoleEntity extends Entity {
    public static final EntityDataAccessor<Boolean> SHOULD_FADE = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.BOOLEAN);
    public int fadeStartTick = 0;

    public BlackHoleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SHOULD_FADE, false);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.entityData.set(SHOULD_FADE, tag.getBoolean("ShouldFade"));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putBoolean("ShouldFade", this.entityData.get(SHOULD_FADE));
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
    public void setShouldFade(boolean shouldFade) {
        this.entityData.set(SHOULD_FADE, shouldFade);
    }
    public boolean shouldFade() {
        return this.entityData.get(SHOULD_FADE);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> eda) {
        super.onSyncedDataUpdated(eda);
        if (eda.equals(SHOULD_FADE)) {
            this.fadeStartTick = this.tickCount;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!shouldFade())
            fadeStartTick = this.tickCount;
        float r = this.getRadius(0.5F);
        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel serverLevel) {
                if (Game2SavedData.getInstance(serverLevel.getServer()).isStopped()) return;
            }
            if (r <= 0.0F) discard();
            for (Entity entity : this.level().getEntities(this, new AABB(this.blockPosition()).inflate(r * r * 4))) {
                if (entity instanceof LivingEntity living && entity.distanceToSqr(this.position()) < r * r * 4) {
                    if (entity instanceof ServerPlayer serverPlayer && (EntitySelector.NO_CREATIVE_OR_SPECTATOR.negate().test(serverPlayer)))
                        continue;
                    Vec3 center = this.getBoundingBox().getCenter();
                    float distance = (float)center.distanceTo(entity.position());
                    if (distance <= r * 3F) {
                        float f = 1.0F - distance / (r * 3F);
                        float scale = f * f * f * f * 0.25F;
                        Vec3 diff = center.subtract(entity.position()).scale(scale);
                        entity.push(diff.x, diff.y, diff.z);
                        if (entity instanceof ServerPlayer player) {
                            player.connection.send(new ClientboundSetEntityMotionPacket(player));
                        }
                        if (tickCount % 10 == 0 && distance <= r) {
                            living.hurt(level().damageSources().genericKill(), 1000F);
                        }

                        entity.fallDistance = 0.0F;
                    }
                }
            }
        } else {
            ShakeHandler.shake(0,new Vec2(1,1), 30, 0);
            if (!ClientGame2Data.blackHoleExist) {
                ClientGame2Data.blackAppearTime = Util.getMillis();
                ClientGame2Data.blackHoleExist = true;
            }
        }
    }

    @Override
    public void onClientRemoval() {
        ClientGame2Data.blackHoleExist = false;
        super.onClientRemoval();
        ClientGame2Data.blackHoleExist = false;
        ClientGame2Data.blackDisappearTime = Util.getMillis();
    }

    public float getRadius(float partialTicks) {
        boolean shouldFade = this.shouldFade();
        if (!shouldFade) {
            return (this.tickCount + partialTicks) / 30F;
        } else {
            float originR = fadeStartTick / 30F;
            return originR * Easing.IN_OUT_ELASTIC.calculate(Mth.clamp(1F - (this.tickCount - this.fadeStartTick + partialTicks) / 60F, 0.0F, 1.0F));
        }
    }
}
