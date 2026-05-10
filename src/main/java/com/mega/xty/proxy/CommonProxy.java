package com.mega.xty.proxy;

import com.mega.endinglib.api.capability.ELCapabilityManager;
import com.mega.endinglib.api.capability.annotation.AutoCapGetter;
import com.mega.endinglib.api.capability.annotation.AutoCapManager;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.capability.*;
import com.mega.xty.common.command.argument.LimbArgumentType;
import com.mega.xty.common.component.ComponentInit;
import com.mega.xty.common.init.*;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@AutoCapManager
public class CommonProxy implements ModProxy {
    public static LazyOptional<Capability<XtyModPlayerCapability>> PLAYER_CAP = LazyOptional.of(() -> {
        return ELCapabilityManager.getCapability(XtyModPlayerCapability.NAME.toString());
    });
    public static LazyOptional<Capability<Map2Capability>> MAP2_CAP = LazyOptional.of(() -> {
        return ELCapabilityManager.getCapability(Map2Capability.NAME.toString());
    });
    public static LazyOptional<Capability<FpsCapability>> FPS_CAP = LazyOptional.of(() -> {
        return ELCapabilityManager.getCapability(FpsCapability.NAME.toString());
    });
    public static LazyOptional<Capability<InteractionCapability>> INTERACTION_CAP = LazyOptional.of(() -> {
        return ELCapabilityManager.getCapability(InteractionCapability.NAME.toString());
    });
    public static LazyOptional<Capability<WeaponWarehouseCapability>> WEAPON_WAREHOUSE_CAP = LazyOptional.of(() -> {
        return ELCapabilityManager.getCapability(WeaponWarehouseCapability.NAME.toString());
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

    public CommonProxy() {
        IEventBus modBus = this.getModBus();
        BlockInit.BLOCKS.register(modBus);
        BlockInit.BLOCK_ENTITIES.register(modBus);
        EntityInit.ENTITIES.register(modBus);
        ItemInit.ITEMS.register(modBus);
        ParticleInit.PARTICLE_TYPES.register(modBus);
        SoundsInit.SOUNDS.register(modBus);
        ComponentInit.init();
        ModCommandArgumentTypes.init(modBus);
        CT.register(modBus);
        modBus.addListener(this::onCommonFMLSetup);
        modBus.addListener(this::setAttributes);
    }

    @AutoCapGetter(XtyModPlayerCapability.class)
    public static LazyOptional<XtyModPlayerCapability> getXtyCap(Player player) {
        return player.getCapability(PLAYER_CAP.orElse(ELCapabilityManager.getCapability(XtyModPlayerCapability.NAME.toString())));
    }

    @AutoCapGetter(Map2Capability.class)
    public static LazyOptional<Map2Capability> getMap2Cap(Player player) {
        return player.getCapability(MAP2_CAP.orElse(ELCapabilityManager.getCapability(Map2Capability.NAME.toString())));
    }

    @AutoCapGetter(FpsCapability.class)
    public static LazyOptional<FpsCapability> getFPSCap(Player player) {
        return player.getCapability(FPS_CAP.orElse(ELCapabilityManager.getCapability(FpsCapability.NAME.toString())));
    }

    @AutoCapGetter(InteractionCapability.class)
    public static LazyOptional<InteractionCapability> getInteractionCap(Interaction interaction) {
        return interaction.getCapability(INTERACTION_CAP.orElse(ELCapabilityManager.getCapability(InteractionCapability.NAME.toString())));
    }

    @AutoCapGetter(WeaponWarehouseCapability.class)
    public static LazyOptional<WeaponWarehouseCapability> getWeaponWarehouseCap(Player player) {
        return player.getCapability(WEAPON_WAREHOUSE_CAP.orElse(ELCapabilityManager.getCapability(WeaponWarehouseCapability.NAME.toString())));
    }
    private void onCommonFMLSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(()-> {
            ELCapabilityManager.regsterCapability(XtyModPlayerCapability::new, new CapabilityToken<XtyModPlayerCapability>() {
            });
            ELCapabilityManager.regsterCapability(Map2Capability::new, new CapabilityToken<Map2Capability>() {
            });
            ELCapabilityManager.regsterCapability(FpsCapability::new, new CapabilityToken<FpsCapability>() {
            });
            ELCapabilityManager.regsterCapability(WeaponWarehouseCapability::new, new CapabilityToken<WeaponWarehouseCapability>() {
            });
            ELCapabilityManager.regsterCapability(InteractionCapability::new, new CapabilityToken<InteractionCapability>() {
            });
            ArgumentTypeInfos.registerByClass(LimbArgumentType.class, ModCommandArgumentTypes.LIMBS.get());
        });
    }
    @SubscribeEvent
    public void setAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityInit.SHADOW_PLAYER.get(), Mob.createMobAttributes().build());
    }
}
