package com.mega.xty;

import com.mega.xty.common.proxy.ClientProxy;
import com.mega.xty.common.proxy.CommonProxy;
import com.mega.xty.common.proxy.ModProxy;
import com.mega.xty.common.proxy.ServerProxy;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(XtyMegaMod.MODID)
public class XtyMegaMod {
    public static final String MODID = "xtymegamod";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ModProxy PROXY = DistExecutor.unsafeRunForDist(()-> ClientProxy::new, ()-> ServerProxy::new);
    public XtyMegaMod() {
        new CommonProxy();
    }
}
