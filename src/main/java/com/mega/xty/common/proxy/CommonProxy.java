package com.mega.xty.common.proxy;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.init.BlockInit;
import com.mega.xty.common.init.ItemInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CommonProxy implements ModProxy {
    public static final DeferredRegister<CreativeModeTab> CT = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, XtyMegaMod.MODID);
    public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = CT.register("main", () ->
            CreativeModeTab.builder()
                    .icon(Items.GOLD_BLOCK::getDefaultInstance)
                    .title(Component.translatable("creative_mode_tab.main.name"))
                    .displayItems((para, output) -> {
                        output.acceptAll(ItemInit.ITEMS.getEntries().stream()
                                        .map(RegistryObject::get)
                                        .map(Item::getDefaultInstance)
                                        .toList()
                                );
                    })
                    .build()
    );
    public CommonProxy() {
        IEventBus modBus = this.getModBus();
        BlockInit.BLOCKS.register(modBus);
        ItemInit.ITEMS.register(modBus);
        CT.register(modBus);
        modBus.addListener(this::onCommonFMLSetup);
    }
    private void onCommonFMLSetup(final FMLCommonSetupEvent event) {
    }
}
