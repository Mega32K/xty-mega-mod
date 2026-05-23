package com.mega.map.common.init;

import com.mega.map.MegaMod;
import com.mega.map.common.block.IItemBlock;
import com.mega.map.common.item.FillFunctionCreatorItem;
import com.mega.map.common.item.armor.OpticalNanosuit;
import com.mega.map.common.item.armor.WineBottleHat;
import com.mega.map.common.item.fps.AdminRenameCardItem;
import com.mega.map.common.item.fps.BDKItem;
import com.mega.map.common.item.fps.C4BombItem;
import com.mega.map.common.item.fps.RenameCardItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MegaMod.MODID);
    public static final RegistryObject<Item> FILL_FUNCTION_CREATOR = ITEMS.register("fill_function_creator", FillFunctionCreatorItem::new);
    public static final RegistryObject<Item> WINE_BOTTLE_HAT = ITEMS.register("wine_bottle_hat", WineBottleHat::new);
    public static final RegistryObject<Item> OPTICAL_NANOSUIT = ITEMS.register("optical_nanosuit", OpticalNanosuit::new);
    public static final RegistryObject<Item> C4_BOMB = ITEMS.register("c4_bomb", C4BombItem::new);
    public static final RegistryObject<Item> BDK = ITEMS.register("bdk", BDKItem::new);
    public static final RegistryObject<Item> RENAME_CARD = ITEMS.register("rename_card", () -> new RenameCardItem(new Item.Properties()));
    public static final RegistryObject<Item> ADMIN_RENAME_CARD = ITEMS.register("admin_rename_card", () -> new AdminRenameCardItem(new Item.Properties()));
    static {
        BlockInit.BLOCKS.getEntries().forEach(ro -> ITEMS.register(ro.getId().getPath(), ()-> {
            Block block = ro.get();
            if (block instanceof IItemBlock iItemBlock) {
                return iItemBlock.asBlockItem(block);
            } else return new BlockItem(block, new Item.Properties());
        }));
    }
}
