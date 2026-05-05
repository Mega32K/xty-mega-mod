package com.mega.xty.common.item.fps;

import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.S2CPlayerRenamePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdminRenameCardItem extends RenameCardItem {
    public AdminRenameCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!player.isShiftKeyDown())
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        return super.use(level, player, hand);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (!player.getAbilities().instabuild)
                itemStack.shrink(1);
            NetworkHandler.sendToPlayer(new S2CPlayerRenamePacket(serverPlayer.getUUID()), serverPlayer);
        }
        return super.interactLivingEntity(itemStack, player, entity, hand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        lines.add(Component.literal("Shift+右键以更改自己的名称").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("右键玩家以更改玩家名称").withStyle(ChatFormatting.GRAY));
    }
}
