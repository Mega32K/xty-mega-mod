package com.mega.xty.common.proxy;

import com.mega.endinglib.api.client.levelevent.LevelEventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy implements ModProxy {
    public ClientProxy() {
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LevelEventManager.registerLevelEvent(110120, (blockPos, randomSource, i) -> {
                Minecraft mc = Minecraft.getInstance();
                switch (i) {
                    case 0 -> {
                    }
                }
            });
        });
    }
}
