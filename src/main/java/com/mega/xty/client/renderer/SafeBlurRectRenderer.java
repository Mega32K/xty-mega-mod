package com.mega.xty.client.renderer;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import net.minecraft.client.gui.GuiGraphics;

public final class SafeBlurRectRenderer {
    private SafeBlurRectRenderer() {
    }

    public static void render(GuiGraphics graphics, float x, float y, float width, float height, int color, float blurRadius) {
        render(MegaGuiGraphics.of(graphics), x, y, width, height, color, blurRadius);
    }

    public static void render(MegaGuiGraphics graphics, float x, float y, float width, float height, int color, float blurRadius) {
        try {
            BlurRectRenderer.render(graphics, x, y, width, height, color, blurRadius);
        } catch (Throwable throwable) {
            graphics.fill(x, y, x + width, y + height, color);
        }
    }
}
