package com.mega.map.common.block;

import com.mega.endinglib.common.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GoldSpawnpointBlock extends Block implements IItemBlock {
    public static final BooleanProperty ALWAYS_RESET = BooleanProperty.create("always_reset");
    public GoldSpawnpointBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ALWAYS_RESET, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ALWAYS_RESET);
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull Entity entity) {
        if (entity instanceof ServerPlayer player) {
            if (!level.isClientSide) {
                if (canSet(level, player, blockPos, blockState)) {
                    player.setRespawnPosition(level.dimension(), blockPos.above(), 0, true, false);
                    player.sendSystemMessage(Component.translatable("mega.xty.spawnpoint_set").withStyle(ChatFormatting.GOLD), true);
                    PacketHandler.playSound(player, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1F, 1F);
                    if (level instanceof ServerLevel serverLevel)
                        serverLevel.sendParticles(
                                player,
                                ParticleTypes.TOTEM_OF_UNDYING,
                                false,
                                blockPos.getX() + .5F,
                                blockPos.getY()+1.05F,
                                blockPos.getZ() + .5F,
                                40,
                                0.3F,
                                0F,
                                0.1F,
                                0.4F
                        );
                }
            }
        }
        super.stepOn(level, blockPos, blockState, entity);
    }
    private static boolean canSet(Level level, ServerPlayer player, BlockPos blockPos, BlockState state) {
        if (state.getValue(ALWAYS_RESET))
            if (player.tickCount % 20 == 0) return true;
        return player.getRespawnPosition() == null || !player.getRespawnPosition().equals(blockPos.above()) || !player.getRespawnDimension().equals(level.dimension());
    }
    @Override
    public Item.Properties asItemProperties() {
        return new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable BlockGetter blockGetter, @NotNull List<Component> lines, @NotNull TooltipFlag tooltipFlag) {
        lines.add(Component.translatable("block.megamod.gold_spawnpoint_block.lore").withStyle(ChatFormatting.GRAY));
    }
}
