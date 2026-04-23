package com.mega.xty.common.item.fps;

import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.entity.C4Entity;
import com.mega.xty.common.init.ItemInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class BDKItem extends Item {
    public static final int SETTING_DURATION = 4 * 20;
    public BDKItem() {
        super(new Properties().stacksTo(1).durability(-1));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack) {
        return SETTING_DURATION;
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        boolean canStart = false;
        for (C4Entity c4Entity : level.getEntitiesOfClass(C4Entity.class, new AABB(player.blockPosition()).inflate(32F))) {
            if (c4Entity.distanceTo(player) < 0.7F) {
                canStart = true;
                break;
            }
        }
        if (canStart) player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int leftTicks) {
        boolean cancel = true;
        if (!level.isClientSide) {
            if (entity instanceof Player player) {
                for (C4Entity c4Entity : level.getEntitiesOfClass(C4Entity.class, new AABB(player.blockPosition()).inflate(32F))) {
                    if (c4Entity.distanceTo(player) < 0.7F) {
                        cancel = false;
                        break;
                    }
                }
                if (cancel) player.stopUsingItem();
            }
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (!level.isClientSide) {
            if (entity instanceof ServerPlayer player) {
                for (C4Entity c4Entity : level.getEntitiesOfClass(C4Entity.class, new AABB(player.blockPosition()).inflate(32F))) {
                    if (c4Entity.distanceTo(player) < 0.7F) {
                        c4Entity.remove(Entity.RemovalReason.KILLED);
                        FpsSavedData savedData = FpsSavedData.getInstance(player.getServer());
                        savedData.setBombPosition((byte) 0);
                        savedData.setBombExist(false);
                    }
                }
            }
        }
        return super.finishUsingItem(itemStack, level, entity);
    }

    public static float getShearingProgress(Player player, float partialTicks) {
        if (!player.isUsingItem() || !player.getUseItem().is(ItemInit.BDK.get())) return -1F;
        return Mth.clamp((player.getTicksUsingItem() + partialTicks) / SETTING_DURATION, 0, 1.0F);
    }
}
