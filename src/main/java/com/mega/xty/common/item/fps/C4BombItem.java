package com.mega.xty.common.item.fps;

import com.mega.endinglib.api.item.IInvulnerableItem;
import com.mega.endinglib.common.network.PacketHandler;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.entity.C4Entity;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.common.init.SoundsInit;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.options.map2game2.Game2ServerOptionsCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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

public class C4BombItem extends Item implements IInvulnerableItem {
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
        return Game2ServerOptionsCache.CURRENT.getBomb().getPlantDurationTicks();
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
            double plantDistance = Game2SavedData.getInstance(sl.getServer()).getServerOptions().getBomb().getPlantSiteDistance();
            if (!FpsSavedData.getInstance(sl.getServer()).isBombExist())
                canStart = canSetC4(map2SavedData.getPointA(), player.position(), plantDistance) || canSetC4(map2SavedData.getPointB(), player.position(), plantDistance);
        }
        if (canStart) player.startUsingItem(interactionHand);
        if (canStart && !level.isClientSide) {
            playC4Sound(player, SoundsInit.C4_CLICK.get(), 0.65F, 1.0F);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int leftTicks) {
        if (!level.isClientSide) {
            if (entity instanceof ServerPlayer player) {
                Map2SavedData savedData = Map2SavedData.getInstance(player.server);
                double plantDistance = Game2SavedData.getInstance(player.server).getServerOptions().getBomb().getPlantSiteDistance();
                if (!canSetC4(savedData.getPointA(), player.position(), plantDistance) && !canSetC4(savedData.getPointB(), player.position(), plantDistance))
                    player.stopUsingItem();
                else
                    playPlantingKeySound(player, getUseDuration(itemStack) - leftTicks);
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
            savedData.setBombCountdownTicks(Game2SavedData.getInstance(sl.getServer()).getServerOptions().getBomb().getCountdownTicks());
            if (!com.mega.xty.common.data.map2.Game2SavedData.getInstance(sl.getServer()).isStopped()) {
                map2SavedData.setCountdown(0);
                NetworkHandler.sendToAll(new com.mega.xty.common.network.s2c.map2.S2CMap2CountdownPacket(0));
            }
            playC4Sound(player, SoundsInit.C4_PLANT.get(), 1.0F, 1.0F);
            playC4Sound(player, SoundsInit.C4_INITIATE.get(), 0.8F, 1.0F);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }
        return super.finishUsingItem(itemStack, level, entity);
    }
    public static float getSettingProgress(Player player, float partialTicks) {
        if (!player.isUsingItem() || !player.getUseItem().is(ItemInit.C4_BOMB.get())) return -1F;
        return Mth.clamp((player.getTicksUsingItem() + partialTicks) / Game2ServerOptionsCache.CURRENT.getBomb().getPlantDurationTicks(), 0, 1.0F);
    }
    public static boolean canSetC4(@Nullable BlockPos point, Vec3 playerPos) {
        return canSetC4(point, playerPos, C4_SET_DISTANCE);
    }
    public static boolean canSetC4(@Nullable BlockPos point, Vec3 playerPos, double maxDistance) {
        if (point == null) return false;
        return playerPos.distanceTo(point.getCenter()) < maxDistance;
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (selectedIndex == slotIndex) {
                boolean played = stack.getOrCreateTag().getBoolean("played");
                if (!played) {
                    int index = player.getRandom().nextInt(0, 8);
                    switch (index) {
                        case 0 -> PacketHandler.playSound(serverPlayer, SoundsInit.C4_DRAW.get(), 1F, 1F);
                        case 1 -> PacketHandler.playSound(serverPlayer, SoundsInit.C4_DRAW_01.get(), 1F, 1F);
                        case 2 -> PacketHandler.playSound(serverPlayer, SoundsInit.C4_DRAW_02.get(), 1F, 1F);
                        case 3 -> PacketHandler.playSound(serverPlayer, SoundsInit.C4_DRAW_03.get(), 1F, 1F);
                        case 4 -> PacketHandler.playSound(serverPlayer, SoundsInit.C4_DRAW_04.get(), 1F, 1F);
                        case 5 -> PacketHandler.playSound(serverPlayer, SoundsInit.C4_DRAW_05.get(), 1F, 1F);
                        case 6 -> PacketHandler.playSound(serverPlayer, SoundsInit.C4_DRAW_06.get(), 1F, 1F);
                        case 7 -> PacketHandler.playSound(serverPlayer, SoundsInit.C4_DRAW_07.get(), 1F, 1F);
                    }
                    stack.getOrCreateTag().putBoolean("played", true);
                }
            } else stack.getOrCreateTag().putBoolean("played", false);
        }
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
    }

    private static void playPlantingKeySound(ServerPlayer player, int usedTicks) {
        switch (usedTicks) {
            case 8 -> playC4Sound(player, SoundsInit.KEY_PRESS1.get(), 0.75F, 1.0F);
            case 16 -> playC4Sound(player, SoundsInit.KEY_PRESS2.get(), 0.75F, 1.0F);
            case 25 -> playC4Sound(player, SoundsInit.KEY_PRESS3.get(), 0.75F, 1.0F);
            case 34 -> playC4Sound(player, SoundsInit.KEY_PRESS4.get(), 0.75F, 1.0F);
            case 44 -> playC4Sound(player, SoundsInit.KEY_PRESS5.get(), 0.75F, 1.0F);
            case 55 -> playC4Sound(player, SoundsInit.KEY_PRESS6.get(), 0.75F, 1.0F);
            case 66 -> playC4Sound(player, SoundsInit.KEY_PRESS7.get(), 0.75F, 1.0F);
        }
    }
    private static void playC4Sound(LivingEntity entity, SoundEvent soundEvent, float volume, float pitch) {
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundSource.PLAYERS, volume, pitch);
    }
}
