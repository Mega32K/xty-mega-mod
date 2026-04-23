package com.mega.xty.common.item.fps;

import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.entity.C4Entity;
import com.mega.xty.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class C4BombItem extends Item {
    public static final float C4_SET_DISTANCE = 6;
    public static final int SETTING_DURATION = 4 * 20;
    public C4BombItem() {
        super(new Properties().stacksTo(1).fireResistant());
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack) {
        return SETTING_DURATION;
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        boolean canStart = false;
        if (level.isClientSide) {
            if (!ClientFpsData.bombExist) {
                canStart = canSetC4(ClientGameData.pointA, player.position()) || canSetC4(ClientGameData.pointB, player.position());
            }
        } else if (level instanceof ServerLevel sl) {
            Map2SavedData map2SavedData = Map2SavedData.getInstance(sl.getServer());
            if (!FpsSavedData.getInstance(sl.getServer()).isBombExist())
                canStart = canSetC4(map2SavedData.getPointA(), player.position()) || canSetC4(map2SavedData.getPointB(), player.position());
        }
        if (canStart) player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int leftTicks) {
        if (!level.isClientSide) {
            if (entity instanceof ServerPlayer player) {
                Map2SavedData savedData = Map2SavedData.getInstance(player.server);
                if (!canSetC4(savedData.getPointA(), player.position()) && !canSetC4(savedData.getPointB(), player.position()))
                    player.stopUsingItem();
            }
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide && level instanceof ServerLevel sl) {
            C4Entity c4 = new C4Entity(player);
            level.addFreshEntity(c4);
            FpsSavedData savedData = FpsSavedData.getInstance(sl.getServer());
            Map2SavedData map2SavedData = Map2SavedData.getInstance(sl.getServer());
            if (map2SavedData.getPointA() != null) {
                if (map2SavedData.getPointB() != null) {
                    if (player.distanceToSqr(map2SavedData.getPointA().getCenter()) < player.distanceToSqr(map2SavedData.getPointB().getCenter())) {
                        savedData.setBombPosition((byte) 1);
                        savedData.setBombExist(true);
                    } else {
                        savedData.setBombPosition((byte) 2);
                        savedData.setBombExist(true);
                    }
                }
            }
        }
        return super.finishUsingItem(itemStack, level, entity);
    }
    public static float getSettingProgress(Player player, float partialTicks) {
        if (!player.isUsingItem() || !player.getUseItem().is(ItemInit.C4_BOMB.get())) return -1F;
        return Mth.clamp((player.getTicksUsingItem() + partialTicks) / SETTING_DURATION, 0, 1.0F);
    }
    public static boolean canSetC4(@Nullable BlockPos point, Vec3 playerPos) {
        if (point == null) return false;
        return playerPos.distanceTo(point.getCenter()) < C4_SET_DISTANCE;
    }
}
