package com.mega.xty.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mega.endinglib.api.client.cmc.LoreHelper;
import com.mega.endinglib.api.item.IInvulnerableItem;
import com.mega.endinglib.api.item.component.*;
import com.mega.xty.client.overlay.DebugModule;
import com.mega.xty.client.overlay.DebugModules;
import com.mega.xty.client.text.ItemDisplayTooltip;
import com.mega.xty.common.item.component.FillCreatorComponent;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FillFunctionCreatorItem extends Item implements IInvulnerableItem, IDefaultComponentsItem, IDebugItem {
    private final Multimap<Attribute, AttributeModifier> attributes = Util.make(() -> {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(UUID.fromString("b35f9fa2-a20e-4614-bd47-c0536f5cda1d"), "DebugItem modifier", 512, AttributeModifier.Operation.ADDITION));
        return builder.build();
    });
    public static final String INTERACTION = "interaction";
    public FillFunctionCreatorItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack itemStack = context.getItemInHand();
        BlockPos clickPos = context.getClickedPos();
        Level level = context.getLevel();
        if (!level.getBlockState(clickPos).isAir()) {
            if (!level.isClientSide)
                addPartOfLine(itemStack, clickPos, player, 1);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemStack, BlockPos clickPos, Player player) {
        if (player == null) return false;
        Level level = player.level();
        if (!level.getBlockState(clickPos).isAir()) {
            if (!level.isClientSide)
                addPartOfLine(itemStack, clickPos, player, 0);
            return true;
        }
        return super.onBlockStartBreak(itemStack, clickPos, player);
    }
    public void addPartOfLine(ItemStack stack, BlockPos aBlock, Player player, int index) {
        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag interaction = nbt.getCompound(INTERACTION);
        if (interaction.isEmpty()) {
            interaction = new CompoundTag();
            nbt.put(INTERACTION, interaction);
        }
        if (index > 0 && interaction.size() < index) return;
        interaction.put(String.valueOf(index), new IntArrayTag(new int[] {aBlock.getX(), aBlock.getY(), aBlock.getZ()}));
        nbt.put(INTERACTION, interaction);

    }
    @Nullable
    public static BlockPos fromArray(int... ints) {
        if (ints.length > 2)
            return new BlockPos(ints[0], ints[1], ints[2]);
        else if (ints.length > 0)
            return new BlockPos(ints[0], ints[0], ints[0]);
        else return null;
    }
    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack self, @NotNull ItemStack other, @NotNull Slot slot, @NotNull ClickAction clickAction, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        if (other.getItem() instanceof BlockItem blockItem) {
            FillCreatorComponent component = ItemComponentManager.get(self, CommonProxy.FILL_CREATOR);
            if (component != null) {
                component.setUseBlock(self, blockItem.getBlock());
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(self, other, slot, clickAction, player, slotAccess);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> lines, @NotNull TooltipFlag tooltipFlag) {
        CompoundTag nbt = itemStack.getTag();
        if (nbt != null) {
            CompoundTag interaction = nbt.getCompound(INTERACTION);
            if (!interaction.isEmpty()) {
                BlockPos start = fromArray(interaction.getIntArray("0"));
                BlockPos end = fromArray(interaction.getIntArray("1"));
                if (start != null)
                    lines.add(Component.translatable("item.xtymegamod.fill_function_creator.lore.selected_start", LoreHelper.blockPos(start)).withStyle(ChatFormatting.GRAY));
                if (end != null)
                    lines.add(Component.translatable("item.xtymegamod.fill_function_creator.lore.selected_end", LoreHelper.blockPos(end)).withStyle(ChatFormatting.GRAY));
            }
            FillCreatorComponent component = ItemComponentManager.get(itemStack, CommonProxy.FILL_CREATOR);
            if (component != null) {
                if (component.use().isEmpty()) {
                    lines.add(Component.translatable("item.xtymegamod.fill_function_creator.lore.use").withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        FillCreatorComponent component = ItemComponentManager.get(stack, CommonProxy.FILL_CREATOR);
        if (component != null) {
            if (component.use().isPresent()) {
                return Optional.of(new ItemDisplayTooltip(
                        component.use().get().asItem().getDefaultInstance()
                ));
            }
        }
        return super.getTooltipImage(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void defaultComponents(Item item, ComponentChanges.Builder builder) {
        builder.add(CommonProxy.FILL_CREATOR, new FillCreatorComponent(List.of(), List.of(), Optional.empty()));
    }
    @Override
    public DebugModule debug() {
        return DebugModules.FILL_FUNCTION_CREATOR;
    }
}
