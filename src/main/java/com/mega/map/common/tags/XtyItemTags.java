package com.mega.map.common.tags;

import com.mega.map.MegaMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

public class XtyItemTags extends Tags.Items {

    public static final TagKey<Item> HAS_GD656_KILL_ICON = tag("has_gd656_killicon");

    private static TagKey<Item> tag(String name)
    {
        return ItemTags.create(new ResourceLocation(MegaMod.MODID, name));
    }
}
