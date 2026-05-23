package com.mega.map.client.overlay;

import com.mega.map.common.data.map2.ClientGameData;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Interaction;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class InteractionTooltipOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        Minecraft mc = Minecraft.getInstance();
        if (ClientGameData.pickedEntity instanceof Interaction interaction) {
            gui.setupOverlayRenderState(true, false);
            Font font = gui.getFont();
            CommonProxy.getInteractionCap(interaction).ifPresent(cap -> {
                cap.tooltip().ifPresent(context -> {
                    guiGraphics.renderTooltip(font, font.split(context, 200), screenWidth / 2 + 32, screenHeight / 2 + 24);
                });
            });
        }
    }
}
