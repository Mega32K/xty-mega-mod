package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.mixin.AccessorPlayerTabOverlay;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class TextTipOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        Component text = ClientGameData.rightTopTextTip;
        if (text == null) return;
        gui.setupOverlayRenderState(true, false);
        Font font = gui.getFont();
        MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
        int y = 2;
        for (var formattedText : font.split(text, 1000)) {
            graphics.drawString(font, formattedText, screenWidth - font.width(formattedText) - 2, y, 0xFFFFFFFF);
            y += font.lineHeight;
        }
    }
}
