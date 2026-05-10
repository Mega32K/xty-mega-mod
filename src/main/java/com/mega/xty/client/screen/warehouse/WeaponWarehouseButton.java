package com.mega.xty.client.screen.warehouse;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.XtyMegaMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WeaponWarehouseButton extends Button {
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/fps/gui_icons.png");
    private boolean accent;

    public WeaponWarehouseButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public WeaponWarehouseButton setAccent(boolean accent) {
        this.accent = accent;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int text = this.active ? (this.accent ? 0xFFF4D75E : 0xFFEAF3FF) : 0xFF748293;
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        graphics.blitNineSliced(ICONS, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        renderString(graphics, text);
    }
    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }

        return 96 + i * 20;
    }
    private void renderString(GuiGraphics graphics, int color) {
        Font font = net.minecraft.client.Minecraft.getInstance().font;
        int leftPadding = this.active ? 4 : 22;
        int available = Math.max(1, getWidth() - leftPadding - 4);
        String text = getMessage().getString();
        int width = font.width(text);
        float scale = width > available ? Math.max(0.65F, (float) available / (float) width) : 1.0F;
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(getX() + leftPadding + available / 2F, getY() + (getHeight() - font.lineHeight * scale) / 2F, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawCenteredString(font, text, 0, 0, color | Mth.ceil(this.alpha * 255.0F) << 24);
        pose.popPose();
    }
}
