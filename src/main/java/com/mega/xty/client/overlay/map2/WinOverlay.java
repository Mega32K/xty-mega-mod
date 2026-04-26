package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.renderer.BlurRectRenderer;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class WinOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (!ClientGameData.map2Playing()) return;

    }
}
