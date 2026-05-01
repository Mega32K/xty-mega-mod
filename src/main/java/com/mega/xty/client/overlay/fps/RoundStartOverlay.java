package com.mega.xty.client.overlay.fps;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.font.ErrorFont;
import com.mega.xty.client.renderer.BlurRectRenderer;
import com.mega.xty.common.data.fps.RoundStartData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class RoundStartOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (!RoundStartData.shouldRenderRoundStart()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        gui.setupOverlayRenderState(true, false);
        renderRoundStart(gui, MegaGuiGraphics.of(guiGraphics), player, screenWidth, screenHeight, partialTick);
    }

    private static void renderRoundStart(ForgeGui gui, MegaGuiGraphics graphics, LocalPlayer player, int screenWidth, int screenHeight, float partialTicks) {
        float alpha = RoundStartData.getRoundStartNotificationAlpha(partialTicks);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 100);
        Font font = gui.getFont();
        String text1 = "回合即将开始";
        String text2 = "倒计时" + RoundStartData.getDisplayCountdownSeconds(partialTicks) + "秒";
        float width = Math.max(font.width(text1), font.width(text2)) + 24.0F;
        float realWidth = alpha * width;
        float height = font.lineHeight * 2.0F + 10.0F;
        float x = screenWidth / 2.0F;
        float y = screenHeight * 0.62F;
        int textColor = ((int) (alpha * 255.0F) << 24) | 0x00D0D0D0;
        int borderColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), textColor);

        renderNotificationBackground(graphics, x, y, realWidth, height, alpha, borderColor);
        graphics.enableScissor((int) (x - realWidth / 2.0F), (int) y, (int) (x + realWidth / 2.0F), (int) (y + height));
        Font renderFont = realWidth < width ? ErrorFont.INSTANCE : font;
        graphics.drawCenteredString(renderFont, text1, (int) x, (int) (y + 4.0F), textColor);
        graphics.drawCenteredString(renderFont, text2, (int) x, (int) (y + 6.0F + font.lineHeight), textColor);
        graphics.disableScissor();
        renderWhiteFlash(graphics, x, y, realWidth, height, alpha);
        poseStack.popPose();
    }

    private static void renderNotificationBackground(MegaGuiGraphics graphics, float centerX, float y, float realWidth, float height, float alpha, int borderColor) {
        graphics.flush();
        BlurRectRenderer.render(graphics, centerX - realWidth / 2.0F, y, realWidth, height, ((int)(alpha * 80.0F + 1.0F) << 24) | 0x00303030, alpha * 8.0F);
        graphics.fill(centerX - 2.0F - realWidth / 2.0F, y, centerX - realWidth / 2.0F, y + height, borderColor);
        graphics.fill(centerX + realWidth / 2.0F, y, centerX + realWidth / 2.0F + 2.0F, y + height, borderColor);
    }

    private static void renderWhiteFlash(MegaGuiGraphics graphics, float centerX, float y, float realWidth, float height, float alpha) {
        if (RoundStartData.roundStartRenderTimer >= RoundStartData.ROUND_START_PROMPT_DURATION - 10) {
            graphics.fill(centerX - realWidth / 2.0F, y, centerX + realWidth / 2.0F, y + height, ((int)(255.0F - alpha * 255.0F) << 24) | 0x00FFFFFF);
        }
    }
}
