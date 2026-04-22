package com.mega.xty.common.item.component;

import com.mega.xty.common.init.ItemInit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class C4BombItem extends Item {
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
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(itemstack);
    }
    public static float getSettingProgress(Player player, float partialTicks) {
        if (!player.isUsingItem() || !player.getUseItem().is(ItemInit.C4_BOMB.get())) return -1F;
        return Mth.clamp((player.getTicksUsingItem() + partialTicks) / SETTING_DURATION, 0, 1.0F);
    }
}
