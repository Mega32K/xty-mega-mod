package com.mega.map;

import com.mega.map.common.config.CommonConfig;
import com.mega.map.common.event.map1.TaczCommonEvents;
import com.mega.map.common.event.map2.Game2TaczEvents;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.proxy.ClientProxy;
import com.mega.map.proxy.CommonProxy;
import com.mega.map.proxy.ModProxy;
import com.mega.map.proxy.ServerProxy;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(MegaMod.MODID)
public class MegaMod {
    public static final String MODID = "megamod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ModProxy PROXY = DistExecutor.unsafeRunForDist(()-> ClientProxy::new, ()-> ServerProxy::new);
    public MegaMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "megamod/megamod-common.toml");
        new CommonProxy();
        NetworkHandler.registerPackets();
        if (ModList.get().isLoaded("tacz")) {
            MinecraftForge.EVENT_BUS.register(Game2TaczEvents.class);
            MinecraftForge.EVENT_BUS.register(TaczCommonEvents.class);
        }
    }
}
