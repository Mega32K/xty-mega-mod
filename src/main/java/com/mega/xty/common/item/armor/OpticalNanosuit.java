package com.mega.xty.common.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.mega.endinglib.api.item.IDragonLightRendererItem;
import com.mega.xty.XtyMegaMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class OpticalNanosuit extends ArmorItem implements IDragonLightRendererItem {
    private static final ArmorMaterial material = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(@NotNull Type type) {
            return -1;
        }

        @Override
        public int getDefenseForType(@NotNull Type type) {
            return 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 0;
        }

        @Override
        public @NotNull SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_IRON;
        }

        @Override
        public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of();
        }

        @Override
        public @NotNull String getName() {
            return "";
        }

        @Override
        public float getToughness() {
            return 0;
        }

        @Override
        public float getKnockbackResistance() {
            return 0;
        }
    };
    public OpticalNanosuit() {
        super(material, Type.CHESTPLATE, new Properties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return XtyMegaMod.MODID + ":textures/models/armor/optical_nanosuit.png";
    }

    @Override
    public boolean enableDragonLightRenderer(ItemStack itemStack) {
        return true;
    }

    @Override
    public int dragonRendererStartColor(ItemStack stack) {
        return 0x6060aef3;
    }

    @Override
    public int dragonRendererEndColor(ItemStack stack) {
        return 0x007b7de9;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> list, @NotNull TooltipFlag tooltipFlag) {
        list.add(Component.literal("穿戴后启用光学隐身系统").withStyle(ChatFormatting.GRAY));
        list.add(
                Component.literal("")
                        .append(Component.literal("  | ").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
                        .append(Component.literal("受到"))
                        .append(Component.literal("摔落伤害").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append("时不再发出声音")
                        .withStyle(ChatFormatting.GRAY));
        list.add(
                Component.literal("")
                        .append(Component.literal("  | ").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
                        .append("从方块上摔落不再产生粒子效果")
                        .withStyle(ChatFormatting.GRAY));
        list.add(
                Component.literal("")
                        .append(Component.literal("  | ").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
                        .append("玩家在")
                        .append(Component.literal("潜行或静止").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal("状态下将进入隐身状态"))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.CHEST) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("578d8d44-37ab-4f63-9486-d02073aa8e1d"), "", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }
}
