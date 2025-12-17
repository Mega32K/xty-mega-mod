package com.mega.xty.common.item.armor;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.api.item.component.ComponentChanges;
import com.mega.endinglib.api.item.component.DataComponents;
import com.mega.endinglib.api.item.component.IDefaultComponentsItem;
import com.mega.endinglib.api.item.component.type.EquippableComponent;
import com.mega.xty.common.entity.BindingEntity;
import com.mega.xty.common.init.EntityInit;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class WineBottleHat extends Item implements IDefaultComponentsItem {
    public static final String TAG = "wineBottleWearing";
    public WineBottleHat() {
        super(new Properties().rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public void defaultComponents(Item item, ComponentChanges.Builder builder) {
        builder.add(DataComponents.EQUIPPABLE, new EquippableComponent(EquipmentSlot.HEAD, Holder.direct(SoundEvents.BOTTLE_FILL), Optional.empty(), Optional.empty(), Optional.empty(), true, true, false, true, false, Holder.direct(SoundEvents.SNOW_GOLEM_SHEAR)));
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            if (!player.getTags().contains(TAG)) {
                player.addTag(TAG);
                BindingEntity bindingEntity = new BindingEntity(EntityInit.BINDING.get(), level);
                bindingEntity.setOwnerUUID(player.getUUID());
                CompoundTag itemTag = stack.getTag();
                if (itemTag != null && CompoundTagUtils.containsString(itemTag, "FlagCommand"))
                    bindingEntity.setFlagCommand(itemTag.getString("FlagCommand"));
                EntityDataAccessor dataAccessor = new EntityDataAccessor(bindingEntity);
                CompoundTag tag = dataAccessor.getData();
                tag.putFloat("width", 0.15f);
                tag.putFloat("height", 0.5f);
                try {
                    dataAccessor.setData(tag);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
                level.addFreshEntity(bindingEntity);
            }
        }
    }
}
