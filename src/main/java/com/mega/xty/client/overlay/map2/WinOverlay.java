package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.renderer.BlurRectRenderer;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Optional;

public class WinOverlay implements IGuiOverlay {
    private static final float MVP_WIDTH_SCALE = 1.25F;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (!ClientFpsData.shouldRenderRoundWin()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        gui.setupOverlayRenderState(true, false);
        MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
        float titleAlpha = ClientFpsData.getRoundWinNotificationAlpha(partialTick);
        if (titleAlpha <= 0.0F) return;
        float mvpAlpha = ClientFpsData.getRoundWinMvpNotificationAlpha(partialTick);

        Font font = gui.getFont();
        float width = Math.max(screenWidth * 0.25F, 160.0F);
        float mvpWidth = width * MVP_WIDTH_SCALE;
        float x = screenWidth / 2.0F;
        float titleHeight = font.lineHeight * 2.0F + 12.0F;
        float mvpHeight = font.lineHeight * 2.0F + 14.0F;
        float titleY = screenHeight * 0.18F;
        float mvpY = titleY + titleHeight + 5.0F;
        int titleTextColor = ((int) (titleAlpha * 255.0F) << 24) | 0x00D0D0D0;
        int titleBorderColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), titleTextColor);

        renderTitleBox(graphics, font, "回合胜利", x, titleY, width, titleHeight, titleAlpha, titleTextColor, titleBorderColor);
        if (mvpAlpha > 0.0F) {
            int mvpTextColor = ((int) (mvpAlpha * 255.0F) << 24) | 0x00D0D0D0;
            int mvpBorderColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), mvpTextColor);
            findMvp(mc).ifPresent(mvp -> renderMvpBox(graphics, font, mvp, x, mvpY, mvpWidth, mvpHeight, mvpAlpha, mvpTextColor, mvpBorderColor));
        }
    }

    private static void renderTitleBox(MegaGuiGraphics graphics, Font font, String text, float centerX, float y, float width, float height, float alpha, int textColor, int borderColor) {
        float realWidth = alpha * width;
        renderNotificationBackground(graphics, centerX, y, realWidth, height, alpha, borderColor);
        graphics.enableScissor((int) (centerX - realWidth / 2.0F), (int) y, (int) (centerX + realWidth / 2.0F), (int) (y + height));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX - font.width(text), y + (height - font.lineHeight * 2.0F) / 2.0F, 0.0F);
        poseStack.scale(2.0F, 2.0F, 1.0F);
        graphics.drawString(font, text, 0, 0, textColor, true);
        poseStack.popPose();
        graphics.disableScissor();
        renderWhiteFlash(graphics, centerX, y, realWidth, height, alpha);
    }

    private static void renderMvpBox(MegaGuiGraphics graphics, Font font, PlayerInfo playerInfo, float centerX, float y, float width, float height, float alpha, int textColor, int borderColor) {
        float realWidth = alpha * width;
        renderNotificationBackground(graphics, centerX, y, realWidth, height, alpha, borderColor);
        graphics.enableScissor((int) (centerX - realWidth / 2.0F), (int) y, (int) (centerX + realWidth / 2.0F), (int) (y + height));

        float left = centerX - width / 2.0F;
        float headSize = font.lineHeight * 2.0F;
        float contentX = left + 12.0F;
        float headCenterX = contentX + headSize / 2.0F;
        float headCenterY = y + height / 2.0F;
        SelectPlayerOverlay.renderProfileIcon(playerInfo.getSkinLocation(), graphics, headCenterX, headCenterY, headSize, false);

        KAD kad = ClientFpsData.getPlayerKAD(playerInfo).getOrDefaultKAD(KAD.KAD_CURRENT);
        float textX = contentX + headSize + 9.0F;
        float textWidth = left + width - 12.0F - textX;
        Component name = ClientFpsData.getPlayerName(playerInfo);
        int killTextColor = (textColor & 0xFF000000) | 0x00FFD54F;
        drawFittedString(graphics, font, name, textX, y + 7.0F, textWidth, textColor);
        drawFittedString(graphics, font, Component.literal("本局击杀 " + kad.kills), textX, y + height - font.lineHeight - 7.0F, textWidth, killTextColor);

        graphics.disableScissor();
        renderWhiteFlash(graphics, centerX, y, realWidth, height, alpha);
    }

    private static void renderNotificationBackground(MegaGuiGraphics graphics, float centerX, float y, float realWidth, float height, float alpha, int borderColor) {
        graphics.flush();
        BlurRectRenderer.render(graphics, centerX - realWidth / 2.0F, y, realWidth, height, ((int)(alpha * 80.0F + 1.0F) << 24) | 0x00303030, alpha * 8.0F);
        graphics.fill(centerX - 2.0F - realWidth / 2.0F, y, centerX - realWidth / 2.0F, y + height, borderColor);
        graphics.fill(centerX + realWidth / 2.0F, y, centerX + realWidth / 2.0F + 2.0F, y + height, borderColor);
    }

    private static void renderWhiteFlash(MegaGuiGraphics graphics, float centerX, float y, float realWidth, float height, float alpha) {
        if (ClientFpsData.roundWinRenderTimer >= ClientFpsData.ROUND_WIN_PROMPT_DURATION - 10) {
            graphics.fill(centerX - realWidth / 2.0F, y, centerX + realWidth / 2.0F, y + height, ((int)(255.0F - alpha * 255.0F) << 24) | 0x00FFFFFF);
        }
    }

    private static void drawFittedString(MegaGuiGraphics graphics, Font font, Component text, float x, float y, float width, int color) {
        float textScale = Math.min(1.0F, width / Math.max(1.0F, font.width(text)));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0F);
        poseStack.scale(textScale, textScale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, true);
        poseStack.popPose();
    }

    private static Optional<PlayerInfo> findMvp(Minecraft minecraft) {
        if (minecraft.player == null) {
            return Optional.empty();
        }
        return minecraft.player.connection.getListedOnlinePlayers().stream()
                .max(ClientFpsData.PLAYER_COMPARATOR);
    }
}
