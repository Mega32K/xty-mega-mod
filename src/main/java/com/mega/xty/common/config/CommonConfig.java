package com.mega.xty.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.ConfigValue<Boolean> GD656_KILLICON_ONLY_TACZ;
    public static boolean killiconOnlyTacz = true;

    static {
        GD656_KILLICON_ONLY_TACZ = BUILDER.comment("只允许gd656killicon再使用tacz武器时启用").define("KilliconOnlyTacz", false);
        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        update();
    }

    public static void update() {
        if (SPEC.isLoaded()) {
            killiconOnlyTacz = GD656_KILLICON_ONLY_TACZ.get();
        }
    }
}
