package com.mega.xty;

import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.proxy.ClientProxy;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.proxy.ModProxy;
import com.mega.xty.proxy.ServerProxy;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(XtyMegaMod.MODID)
public class XtyMegaMod {
    public static final String MODID = "xtymegamod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ModProxy PROXY = DistExecutor.unsafeRunForDist(()-> ClientProxy::new, ()-> ServerProxy::new);
    public XtyMegaMod() {
        new CommonProxy();
        NetworkHandler.registerPackets();
    }
}
