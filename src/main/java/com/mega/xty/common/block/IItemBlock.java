package com.mega.xty.common.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface IItemBlock {
    default Item.Properties asItemProperties() {
        return new Item.Properties();
    }
    default BlockItem asBlockItem(Block block) {
        return new BlockItem(block, this.asItemProperties());
    }
}
