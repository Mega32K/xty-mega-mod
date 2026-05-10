package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.font.ErrorFont;
import com.mega.xty.client.renderer.BlurRectRenderer;
import com.mega.xty.client.shader.ModShaders;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.item.fps.BDKItem;
import com.mega.xty.common.item.fps.C4BombItem;
import com.mega.xty.proxy.ClientProxy;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class C4Overlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (ClientGame2Data.isStopped) return;

        gui.setupOverlayRenderState(true, false);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
        if (ClientFpsData.bombExist) {
            float progress = BDKItem.getShearingProgress(mc.player, partialTick);
            if (progress >= 0.0F && canRenderBombProgressBar(mc.player)) {
                renderShearingAnimation(mc.player, gui, graphics, progress, screenWidth, screenHeight);
            }
            if (ClientFpsData.shouldRenderBombCountdown() && canRenderBombCountdownPrompt(mc.player)) {
                renderBombCountdown(mc.player, gui, graphics, ClientFpsData.bombCountdownRenderTicks, screenWidth, screenHeight, partialTick);
            }
        } else {
            float progress = C4BombItem.getSettingProgress(mc.player, partialTick);
            if (progress >= 0.0F && canRenderBombProgressBar(mc.player)) {
                renderBombSettingAnimation(mc.player, gui, graphics, progress, screenWidth, screenHeight);
            }
        }
    }

    public void renderBombCountdown(LocalPlayer player, ForgeGui gui, MegaGuiGraphics graphics, int countdownTicks, int screenWidth, int screenHeight, float partialTicks) {
        float alpha = Easing.OUT_CUBIC.calculate(Math.min(1.0F, Math.min(ClientFpsData.bombCountdownRenderTimer - partialTicks, 10.0F) / 10.0F))
                * Easing.OUT_CUBIC.calculate(Math.min(1.0F, (ClientFpsData.BOMB_COUNTDOWN_PROMPT_DURATION - ClientFpsData.bombCountdownRenderTimer + partialTicks) / 10.0F));

        PoseStack poseStack = graphics.pose();
        poseStack.translate(0.0F, 0.0F, 100.0F);
        poseStack.pushPose();

        Font font = gui.getFont();
        String text1 = "炸弹已被安放";
        String text2 = "离被引爆还剩" + ClientFpsData.getDisplayBombSeconds(countdownTicks) + "秒";
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

    public void renderBombSettingAnimation(LocalPlayer player, ForgeGui gui, MegaGuiGraphics graphics, float progress, int screenWidth, int screenHeight) {
        float alpha = 1.0F - Easing.IN_OUT_CUBIC.calculate(1.0F - Math.min(1.0F, progress * 7.0F));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        Font font = gui.getFont();
        int percentColor = ((int) (alpha * 255.0F) << 24) | 0x00A0A0A0;
        float barWidth = screenWidth * 0.3F;
        float barHeight = barWidth * 0.1F;
        float textBarWidth = barWidth * 0.5F;
        float x = (screenWidth - barWidth) / 2.0F;
        float y = screenHeight * 0.75F - 10.0F * (1.0F - alpha);
        float textBarX = (screenWidth - textBarWidth) / 2.0F;
        float textBarY = y - barHeight - 2.0F;
        int teamColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), percentColor);

        float realTextBarWidth = alpha * textBarWidth;
        renderNotificationBackground(graphics, textBarX + textBarWidth / 2.0F, textBarY, realTextBarWidth, barHeight, alpha, teamColor);
        graphics.enableScissor((int) (textBarX + (textBarWidth - realTextBarWidth) / 2.0F), (int) textBarY, (int) (textBarX + (textBarWidth + realTextBarWidth) / 2.0F), (int) (textBarY + barHeight));
        graphics.drawCenteredString(realTextBarWidth < textBarWidth ? ErrorFont.INSTANCE : font, "炸弹安装中", (int) (textBarX + textBarWidth / 2.0F), (int) (textBarY + (barHeight - font.lineHeight) / 2.0F), percentColor);
        graphics.disableScissor();

        BlurRectRenderer.render(graphics, x, y, barWidth, barHeight, ((int) (alpha * 80.0F + 1.0F) << 24) | 0x00303030, alpha * 8.0F);
        float renderProgress = Math.min(1.0F, Math.max(0.0F, progress));
        float progressBarX = x + 2.0F;
        float progressBarY = y + 3.0F;
        float progressBarWidth = barWidth - 4.0F;
        float progressBarHeight = barHeight - 6.0F;
        float progressWidth = progressBarWidth * renderProgress;
        if (progressWidth > 0.0F) {
            float whiteProgress = Math.min(1.0F, Math.max(0.0F, (renderProgress - 0.8F) / 0.2F));
            int baseR = FastColor.ARGB32.red(0xFF8B9EA8);
            int baseG = FastColor.ARGB32.green(0xFF8B9EA8);
            int baseB = FastColor.ARGB32.blue(0xFF8B9EA8);
            float r = (baseR + (255 - baseR) * whiteProgress) / 255.0F;
            float g = (baseG + (255 - baseG) * whiteProgress) / 255.0F;
            float b = (baseB + (255 - baseB) * whiteProgress) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.setColor(r, g, b, alpha);
            ModShaders.logoGlitch((player.tickCount + Minecraft.getInstance().getPartialTick()) / 20.0F, 0.7F + renderProgress * 0.8F);
            graphics.blit(
                    ClientProxy.FPS_UI_ICONS_LOCATION,
                    progressBarX,
                    progressBarY,
                    progressWidth,
                    progressBarHeight,
                    34.0F,
                    1.0F,
                    1.0F,
                    1.0F,
                    512.0F,
                    384.0F,
                    ModShaders::getLogoGlitch
            );
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        graphics.fill(x - 2.0F, y, x, y + barHeight, teamColor);
        graphics.fill(x + barWidth, y, x + barWidth + 2.0F, y + barHeight, teamColor);
        String percentText = Math.round(renderProgress * 100.0F) + "%";
        graphics.drawString(font, percentText, progressBarX + progressBarWidth - font.width(percentText) - 1.0F, progressBarY + (progressBarHeight - font.lineHeight) / 2.0F, percentColor, true);
        poseStack.popPose();
    }

    public void renderShearingAnimation(LocalPlayer player, ForgeGui gui, MegaGuiGraphics graphics, float progress, int screenWidth, int screenHeight) {
        float alpha = 1.0F - Easing.IN_OUT_CUBIC.calculate(1.0F - Math.min(1.0F, progress * 7.0F));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        Font font = gui.getFont();
        int percentColor = ((int) (alpha * 255.0F) << 24) | 0x00A0A0A0;
        float barWidth = screenWidth * 0.3F;
        float barHeight = barWidth * 0.1F;
        float textBarWidth = barWidth * 0.5F;
        float x = (screenWidth - barWidth) / 2.0F;
        float y = screenHeight * 0.75F - 10.0F * (1.0F - alpha);
        float textBarX = (screenWidth - textBarWidth) / 2.0F;
        float textBarY = y - barHeight - 2.0F;
        int teamColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), percentColor);

        float realTextBarWidth = alpha * textBarWidth;
        renderNotificationBackground(graphics, textBarX + textBarWidth / 2.0F, textBarY, realTextBarWidth, barHeight, alpha, teamColor);
        graphics.enableScissor((int) (textBarX + (textBarWidth - realTextBarWidth) / 2.0F), (int) textBarY, (int) (textBarX + (textBarWidth + realTextBarWidth) / 2.0F), (int) (textBarY + barHeight));
        graphics.drawCenteredString(realTextBarWidth < textBarWidth ? ErrorFont.INSTANCE : font, "炸弹拆除中", (int) (textBarX + textBarWidth / 2.0F), (int) (textBarY + (barHeight - font.lineHeight) / 2.0F), percentColor);
        graphics.disableScissor();

        BlurRectRenderer.render(graphics, x, y, barWidth, barHeight, ((int) (alpha * 80.0F + 1.0F) << 24) | 0x00303030, alpha * 8.0F);
        float renderProgress = Math.min(1.0F, Math.max(0.0F, progress));
        float progressBarX = x + 2.0F;
        float progressBarY = y + 3.0F;
        float progressBarWidth = barWidth - 4.0F;
        float progressBarHeight = barHeight - 6.0F;
        float progressWidth = progressBarWidth * renderProgress;
        if (progressWidth > 0.0F) {
            float whiteProgress = Math.min(1.0F, Math.max(0.0F, (renderProgress - 0.8F) / 0.2F));
            int baseR = FastColor.ARGB32.red(0xFF8B9EA8);
            int baseG = FastColor.ARGB32.green(0xFF8B9EA8);
            int baseB = FastColor.ARGB32.blue(0xFF8B9EA8);
            float r = (baseR + (255 - baseR) * whiteProgress) / 255.0F;
            float g = (baseG + (255 - baseG) * whiteProgress) / 255.0F;
            float b = (baseB + (255 - baseB) * whiteProgress) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.setColor(r, g, b, alpha);
            ModShaders.logoGlitch((player.tickCount + Minecraft.getInstance().getPartialTick()) / 20.0F, 0.7F + renderProgress * 0.8F);
            graphics.blit(
                    ClientProxy.FPS_UI_ICONS_LOCATION,
                    progressBarX,
                    progressBarY,
                    progressWidth,
                    progressBarHeight,
                    34.0F,
                    1.0F,
                    1.0F,
                    1.0F,
                    512.0F,
                    384.0F,
                    ModShaders::getLogoGlitch
            );
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        graphics.fill(x - 2.0F, y, x, y + barHeight, teamColor);
        graphics.fill(x + barWidth, y, x + barWidth + 2.0F, y + barHeight, teamColor);
        String percentText = Math.round(renderProgress * 100.0F) + "%";
        graphics.drawString(font, percentText, progressBarX + progressBarWidth - font.width(percentText) - 1.0F, progressBarY + (progressBarHeight - font.lineHeight) / 2.0F, percentColor, false);
        poseStack.popPose();
    }

    private static void renderNotificationBackground(MegaGuiGraphics graphics, float centerX, float y, float realWidth, float height, float alpha, int borderColor) {
        graphics.flush();
        BlurRectRenderer.render(graphics, centerX - realWidth / 2.0F, y, realWidth, height, ((int) (alpha * 80.0F + 1.0F) << 24) | 0x00303030, alpha * 8.0F);
        graphics.fill(centerX - 2.0F - realWidth / 2.0F, y, centerX - realWidth / 2.0F, y + height, borderColor);
        graphics.fill(centerX + realWidth / 2.0F, y, centerX + realWidth / 2.0F + 2.0F, y + height, borderColor);
    }

    private static void renderWhiteFlash(MegaGuiGraphics graphics, float centerX, float y, float realWidth, float height, float alpha) {
        if (ClientFpsData.bombCountdownRenderTimer >= ClientFpsData.BOMB_COUNTDOWN_PROMPT_DURATION - 10) {
            graphics.fill(centerX - realWidth / 2.0F, y, centerX + realWidth / 2.0F, y + height, ((int) (255.0F - alpha * 255.0F) << 24) | 0x00FFFFFF);
        }
    }

    private static boolean canRenderBombCountdownPrompt(LocalPlayer player) {
        return CommonProxy.getFPSCap(player)
                .map(cap -> cap.getGame2ClientOptions().hud().showBombCountdownPrompt())
                .orElse(true);
    }

    private static boolean canRenderBombProgressBar(LocalPlayer player) {
        return CommonProxy.getFPSCap(player)
                .map(cap -> cap.getGame2ClientOptions().hud().showBombProgressBar())
                .orElse(true);
    }
}
