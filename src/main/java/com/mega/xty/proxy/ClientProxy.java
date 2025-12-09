package com.mega.xty.proxy;

import com.mega.endinglib.api.client.levelevent.LevelEventManager;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.RenderUtils;
import com.mega.xty.client.overlay.DebugOverlays;
import com.mega.xty.client.text.ClientItemDisplayTooltip;
import com.mega.xty.client.text.ItemDisplayTooltip;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class ClientProxy implements ModProxy {
    public static final KeyMapping DEBUG_ITEM_GUI = new KeyMapping("key.xtymegamod.debug_item_gui", GLFW.GLFW_KEY_G | GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.xtymegamod");
    public ClientProxy() {
        IEventBus mBus = this.getModBus();
        IEventBus fBus = this.getForgeBus();
        mBus.addListener(this::onKeyRegister);
        mBus.addListener(this::clientSetup);
        mBus.addListener(this::onRegisterOverlays);
        mBus.addListener(this::onRegisterClientTooltipComponent);
        mBus.addListener(this::onShaderRegistering);
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
    private void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(DEBUG_ITEM_GUI);
    }
    private void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("xty_debug", DebugOverlays.INSTANCE);
    }
    private void onRegisterClientTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ItemDisplayTooltip.class, ClientItemDisplayTooltip::new);
    }
    private void onShaderRegistering(RegisterShadersEvent event) {

    }

}
