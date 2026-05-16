package com.mega.xty.client.text;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class ClientItemDisplayTooltip implements ClientTooltipComponent {
    private final ItemDisplayTooltip tooltip;

    public ClientItemDisplayTooltip(ItemDisplayTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return 16 + 11;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(font.width(tooltip.itemStack().getDisplayName()) + 2, 18);
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics g) {
        MegaGuiGraphics graphics = MegaGuiGraphics.of(g);
        float textW = font.width(tooltip.itemStack().getDisplayName());
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.translate(0, 0, 300);
        graphics.renderItem(tooltip.itemStack(), (int) (x + textW * 0.8F / 2F) - 8, y, 3);
        graphics.renderItemDecorations(font, tooltip.itemStack(), x, y);
        stack.popPose();
    }

    @Override
    public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix4f, MultiBufferSource.@NotNull BufferSource bufferSource) {
        MegaGuiGraphics graphics = new MegaGuiGraphics(Minecraft.getInstance(), bufferSource);
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.translate(x, y + 17, 0);
        stack.scale(0.8F, 0.8F, 1F);
        stack.translate(0, 0, 1200);
        graphics.drawString(font, tooltip.itemStack().getDisplayName(), 0, 0, 0xFFFFFFFF);
        stack.popPose();
    }
}
