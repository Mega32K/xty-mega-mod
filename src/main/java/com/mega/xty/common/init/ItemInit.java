package com.mega.xty.common.init;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.block.IItemBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, XtyMegaMod.MODID);
    static {
        BlockInit.BLOCKS.getEntries().forEach(ro -> ITEMS.register(ro.getId().getPath(), ()-> {
            Block block = ro.get();
            if (block instanceof IItemBlock iItemBlock) {
                return iItemBlock.asBlockItem(block);
            } else return new BlockItem(block, new Item.Properties());
        }));
    }
}
