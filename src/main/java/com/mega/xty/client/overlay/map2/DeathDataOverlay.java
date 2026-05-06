package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.data.map2.DeathData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

public class DeathDataOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (!ClientGameData.map2Playing()) return;
        gui.setupOverlayRenderState(true, false);
        Minecraft mc = Minecraft.getInstance();
        MegaGuiGraphics graphics = new MegaGuiGraphics(mc, guiGraphics.bufferSource());
        int guiWidth = graphics.guiWidth();
        Font font = gui.getFont();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, graphics.guiHeight() * 0.1F + 13F, 0);
        graphics.flush();
        for (int i=0;i<ClientGameData.deathDataList.size();i++) {
            DeathData data = ClientGameData.deathDataList.get(i);
            poseStack.pushPose();
            poseStack.translate(guiWidth - data.getWidth(font, data.weapon) - 4, 0, 0);
            var consumer = data.render(graphics, poseStack, font, partialTick);
            poseStack.popPose();
            consumer.accept(poseStack);
        }
        poseStack.popPose();

        RenderSystem.disableBlend();
    }
}
