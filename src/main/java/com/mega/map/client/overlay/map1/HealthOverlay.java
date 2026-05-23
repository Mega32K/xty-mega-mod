package com.mega.map.client.overlay.map1;

import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.map.common.data.map1.ClientGame2Data;
import com.mega.map.proxy.ClientProxy;
import com.mega.map.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class HealthOverlay implements IGuiOverlay {
    static int layerWidth = 186;
    static int layerHeight = 9;
    static int width = 182;
    static int height = 5;
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (ClientGame2Data.isStopped) return;
        Player player = ClientWrapped.clientPlayer();
        Minecraft mc = Minecraft.getInstance();
        ClientLevel clientLevel = mc.level;
        if (player != null && clientLevel != null) {
            if (!gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements())
                CommonProxy.getXtyCap(player).ifPresent(capability -> {
                    gui.setupOverlayRenderState(true, false);
                    MegaGuiGraphics graphics = new MegaGuiGraphics(mc, guiGraphics.bufferSource());
                    int left = screenWidth / 2 - layerWidth / 2;
                    int top = screenHeight - gui.leftHeight;
                    graphics.blit(ClientProxy.ICONS, left, top - layerHeight, 0, 0, layerWidth, layerHeight);

                    float progress = Mth.lerp(partialTick, ClientGame2Data.lastHealth, capability.getGame2Health()) / capability.getGame2MaxHealth();
                    progress = Mth.clamp(progress, 0F, 1F);
                    if (ClientGame2Data.sceneChanging) progress = 1F;
                    graphics.blit(ClientProxy.ICONS, left + (layerWidth - width) / 2F, top - layerHeight + (layerHeight - height) / 2F, width * progress, height, 0, 9, width * progress, height, 256F, 256F);
                    RenderSystem.disableBlend();
                    gui.leftHeight += 9;
                });
        }
    }
}
