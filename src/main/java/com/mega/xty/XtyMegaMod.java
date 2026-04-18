package com.mega.xty;

import com.mega.xty.common.config.CommonConfig;
import com.mega.xty.common.event.map1.Game2TaczEvents;
import com.mega.xty.common.event.map1.TaczCommonEvents;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.proxy.ClientProxy;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.proxy.ModProxy;
import com.mega.xty.proxy.ServerProxy;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(XtyMegaMod.MODID)
public class XtyMegaMod {
    public static final String MODID = "xtymegamod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ModProxy PROXY = DistExecutor.unsafeRunForDist(()-> ClientProxy::new, ()-> ServerProxy::new);
    public XtyMegaMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "xtymegamod/xtymegamod-common.toml");
        new CommonProxy();
        NetworkHandler.registerPackets();
        if (ModList.get().isLoaded("tacz")) {
            MinecraftForge.EVENT_BUS.register(Game2TaczEvents.class);
            MinecraftForge.EVENT_BUS.register(TaczCommonEvents.class);
        }
    }
}
