package com.mega.map.common.init;

import com.mega.endinglib.EndingLibrary;
import com.mega.map.common.command.argument.LimbArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModCommandArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRIES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, EndingLibrary.MODID);
    public static final RegistryObject<ArgumentTypeInfo<LimbArgumentType, ?>> LIMBS = REGISTRIES.register("limbs", () -> SingletonArgumentInfo.contextFree(LimbArgumentType::limb));
    public static void init(IEventBus bus) {
        REGISTRIES.register(bus);
    }
}
