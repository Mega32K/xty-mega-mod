package com.mega.xty.client.screen.warehouse;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class WeaponWarehouseButton extends Button {
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
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        boolean hovered = isHoveredOrFocused();
        int fill = this.accent ? 0xCC21415D : 0xCC171D28;
        int border = this.accent ? 0xFF8AD8FF : 0xFF4E5F73;
        int text = this.active ? 0xFFEAF3FF : 0xFF748293;
        if (hovered) {
            fill = this.accent ? 0xDD2A567D : 0xDD202A36;
            border = this.accent ? 0xFFA9E6FF : 0xFF6C8199;
        }
        graphics.fill(x, y, x + width, y + height, fill);
        graphics.fill(x, y + height - 1, x + width, y + height, border);
        graphics.renderOutline(x, y, width, height, 0x332A3A4D);
        renderString(graphics, text);
    }

    private void renderString(GuiGraphics graphics, int color) {
        Font font = net.minecraft.client.Minecraft.getInstance().font;
        int available = Math.max(1, getWidth() - 8);
        String text = getMessage().getString();
        int width = font.width(text);
        float scale = width > available ? Math.max(0.65F, (float) available / (float) width) : 1.0F;
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(getX() + getWidth() / 2F, getY() + (getHeight() - font.lineHeight * scale) / 2F, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawCenteredString(font, text, 0, 0, color | Mth.ceil(this.alpha * 255.0F) << 24);
        pose.popPose();
    }
}
