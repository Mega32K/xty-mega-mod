package com.mega.map.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public interface ModProxy {
    default IEventBus getModBus() {
        return FMLJavaModLoadingContext.get().getModEventBus();
    }
    default IEventBus getForgeBus() {
        return MinecraftForge.EVENT_BUS;
    }
}
