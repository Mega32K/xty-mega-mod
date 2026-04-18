package com.mega.xty.common.entity;

import com.mega.endinglib.mixin.accessor.AccessorItemEntity;
import com.mega.xty.common.data.map1.ClientGame2Data;
import com.mega.xty.common.data.map1.Game2SavedData;
import com.mega.xty.common.init.EntityInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Objective;

public class Game2ItemEntity extends ItemEntity {
    public float xSpeed = 0F;
    public float ySpeed = 0F;
    public float zSpeed = 0F;
    public boolean xRotate = false;
    public boolean zRotate = false;
    public Game2ItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
        this.xRotate = level.random.nextBoolean();
        this.zRotate = level.random.nextBoolean();
        this.xSpeed = level.random.nextFloat() * 2F;
        this.ySpeed = level.random.nextFloat() * 2F;
        this.zSpeed = level.random.nextFloat() * 2F;
        if (!this.xRotate) this.xSpeed = 0F;
        if (!this.zRotate) this.zSpeed = 0F;
    }
    public Game2ItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(level, x, y, z, stack, level.random.nextDouble() * 0.2D - 0.1D, 0.2D, level.random.nextDouble() * 0.2D - 0.1D);
    }

    public Game2ItemEntity(Level level, double x, double y, double z, ItemStack stack, double mX, double mY, double mZ) {
        this(EntityInit.GAME_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(mX, mY, mZ);
        this.setItem(stack);
        stack.getItem();
        this.lifespan = stack.getEntityLifespan(level);
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level().isClientSide) {
            AccessorItemEntity accessorItem = (AccessorItemEntity) this;
            if (accessorItem.getPickupDelay() > 0) return;
            boolean canTouch = true;
            if (this.level() instanceof ServerLevel sl) {
                Game2SavedData savedData = Game2SavedData.getInstance(sl.getServer());
                if (!savedData.isStopped()) {
                    Objective health =  savedData.getHealthObjective();
                    if (health != null &&
                            sl.getServer().getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), health).getScore() <= 0) {
                        canTouch = false;
                    }
                }
            }
            if (canTouch) {
                ItemStack itemstack = this.getItem();
                Item item = itemstack.getItem();
                ItemStack copy = itemstack.copy();
                if (accessorItem.getPickupDelay() == 0 && (accessorItem.getTarget() == null || accessorItem.getTarget().equals(player.getUUID())) && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, itemstack.copy());
                    itemstack.setCount(0);
                    int i = copy.getCount() - itemstack.getCount();
                    copy.setCount(i);
                    net.minecraftforge.event.ForgeEventFactory.firePlayerItemPickupEvent(player, this, copy);
                    player.take(this, i);
                    if (itemstack.isEmpty()) {
                        this.discard();
                        itemstack.setCount(i);
                    }

                    player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
                    player.onItemPickup(this);
                }
            }
        }
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel sl) {
            if (Game2SavedData.getInstance(sl.getServer()).isSceneChanging())
                return;
        } else if (ClientGame2Data.sceneChanging) return;
        super.tick();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(4.0);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_19883_) {
        return super.shouldRenderAtSqrDistance(p_19883_ / 4F);
    }

    @Override
    public boolean canBeCollidedWith() {
        return super.canBeCollidedWith();
    }
}
