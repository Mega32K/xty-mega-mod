package com.mega.xty.proxy;

import com.mega.endinglib.api.capability.ELCapabilityManager;
import com.mega.endinglib.api.data.TagEnum;
import com.mega.endinglib.api.item.component.ComponentTypeBuilder;
import com.mega.endinglib.api.item.component.DataComponents;
import com.mega.endinglib.api.item.component.ItemComponentType;
import com.mega.endinglib.common.capability.EndingLibraryPlayerCapability;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.capability.XtyModPlayerCapability;
import com.mega.xty.common.init.BlockInit;
import com.mega.xty.common.init.EntityInit;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.common.item.component.FillCreatorComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CommonProxy implements ModProxy {
    public static LazyOptional<Capability<XtyModPlayerCapability>> PLAYER_CAP = LazyOptional.of(() -> {
        return ELCapabilityManager.getCapability(XtyModPlayerCapability.NAME.toString());
    });
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
    public static final ResourceLocation COM_FILL_CREATOR = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "debug/fill_creator");
    public static final ItemComponentType<FillCreatorComponent> FILL_CREATOR = DataComponents.register(
            COM_FILL_CREATOR,
            ComponentTypeBuilder.create(builder -> builder
                    .registryName(COM_FILL_CREATOR)
                    .codec(FillCreatorComponent.CODEC)
                    .rootTagType(TagEnum.LIST)
                    .build()
            )
    );

    public CommonProxy() {
        IEventBus modBus = this.getModBus();
        BlockInit.BLOCKS.register(modBus);
        BlockInit.BLOCK_ENTITIES.register(modBus);
        EntityInit.ENTITIES.register(modBus);
        ItemInit.ITEMS.register(modBus);
        CT.register(modBus);
        modBus.addListener(this::onCommonFMLSetup);
    }


    public static LazyOptional<XtyModPlayerCapability> getXtyCap(Player player) {
        return player.getCapability(PLAYER_CAP.orElse(ELCapabilityManager.getCapability(XtyModPlayerCapability.NAME.toString())));
    }
    private void onCommonFMLSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(()-> {
            ELCapabilityManager.regsterCapability(XtyModPlayerCapability::new, new CapabilityToken<XtyModPlayerCapability>() {
            });
        });
    }
}
