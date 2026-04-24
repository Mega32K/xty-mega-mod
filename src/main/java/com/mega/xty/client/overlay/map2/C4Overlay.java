package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.client.screen.BlitInfo;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.renderer.BlurRectRenderer;
import com.mega.xty.client.shader.ModShaders;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.item.fps.BDKItem;
import com.mega.xty.common.item.fps.C4BombItem;
import com.mega.xty.proxy.ClientProxy;
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
    public static int[] CODE = new int[] {7,3,5,5,6,0,8};
    public static final BlitInfo C4_1 = new BlitInfo(
            ClientProxy.FPS_UI_ICONS_LOCATION,
            256, 0,
            128, 128
    );
    public static final BlitInfo C4_2 = new BlitInfo(
            ClientProxy.FPS_UI_ICONS_LOCATION,
            384, 0,
            128, 128
    );
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {

        if (gui.getMinecraft().options.hideGui) return;
        if (ClientGame2Data.isStopped) return;
        gui.setupOverlayRenderState(true, false);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            //若已安放炸弹
            if (ClientFpsData.bombExist) {
                if (ClientFpsData.shouldRenderBombCountdown()) {
                    renderBombCountdown(mc.player, gui, MegaGuiGraphics.of(guiGraphics), ClientFpsData.bombCountdownRenderTicks, screenWidth, screenHeight, partialTick);
                }
                //若正在拆包
                float progress = BDKItem.getShearingProgress(mc.player, partialTick);
                if (progress >= 0.0F) {
                    renderShearingAnimation(mc.player, gui, MegaGuiGraphics.of(guiGraphics), progress, screenWidth, screenHeight);
                }
            } else {
                //若正在下包
                float progress = C4BombItem.getSettingProgress(mc.player, partialTick);
                if (progress >= 0.0F) {
                    renderBombSettingAnimation(mc.player, gui, MegaGuiGraphics.of(guiGraphics), progress, screenWidth, screenHeight);
                }
            }
        }
    }
    public void renderBombCountdown(LocalPlayer player, ForgeGui gui, MegaGuiGraphics graphics, int countdownTicks, int screenWidth, int screenHeight, float partialTicks) {
        float alpha = Easing.OUT_CUBIC.calculate(Math.min(1.0F, Math.min(ClientFpsData.bombCountdownRenderTimer - partialTicks, 10) / 10.0F)) * Easing.OUT_CUBIC.calculate(Math.min(1.0F, (ClientFpsData.BOMB_COUNTDOWN_PROMPT_DURATION - ClientFpsData.bombCountdownRenderTimer + partialTicks) / 10F));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        Font font = gui.getFont();
        String text1 = "炸弹已被安放";
        String text2 = "离被引爆还剩" + ClientFpsData.getDisplayBombSeconds(countdownTicks) + "秒";
        float width = Math.max(font.width(text1), font.width(text2)) + 24F;
        float realWidth = alpha * width;
        float height = font.lineHeight * 2F + 10F;
        float x = screenWidth / 2F;
        float y = screenHeight * 0.62F;
        int textColor = ((int) (alpha * 255) << 24) | 0x00D0D0D0;
        int borderColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), textColor);
        graphics.flush();
        BlurRectRenderer.render(graphics, x - realWidth / 2F, y, realWidth, height, ((int)(alpha * 80 + 1) << 24 | 0x00300000 | 0x00003000 | 0x00000030), alpha * 8.0F);
        graphics.fill(x - 2 - realWidth / 2F, y, x- realWidth / 2F, y + height, borderColor);
        graphics.fill(x + realWidth / 2F, y, x + realWidth / 2F + 2, y + height, borderColor);
        graphics.enableScissor((int) (x - realWidth / 2F), (int) y, (int) (x + realWidth / 2F), (int) (y + height));
        graphics.drawCenteredString(font, text1, (int) x, (int) (y + 4F), textColor);
        graphics.drawCenteredString(font, text2, (int) x, (int) (y + 6F + font.lineHeight), textColor);
        graphics.disableScissor();
        //模仿CS2的白色淡入淡出
        if (ClientFpsData.bombCountdownRenderTimer >= ClientFpsData.BOMB_COUNTDOWN_PROMPT_DURATION - 10)
            graphics.fill(x - realWidth / 2F, y, x + realWidth / 2F, y + height, ((int)(255 - alpha * 255) << 24 | 0x00FF0000 | 0x0000FF00 | 0x000000FF));

        poseStack.popPose();
    }
    public void renderBombSettingAnimation(LocalPlayer player, ForgeGui gui, MegaGuiGraphics graphics, float progress, int screenWidth, int screenHeight) {
        float alpha = 1F - Easing.IN_OUT_CUBIC.calculate((1F - Math.min(1F, progress * 7F)));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        Font font = gui.getFont();
        int percentColor = ((int)(alpha * 255) << 24) | 0x00A0A0A0;
        float barWidth = screenWidth * 0.3F;
        float barHeight = barWidth * 0.1F;
        float textBarWidth = barWidth * 0.5F;
        float x = (screenWidth - barWidth) / 2;
        float y = screenHeight * 0.75F;
        y += (-10 * (1F - alpha));
        float textBarX = (screenWidth - textBarWidth) / 2;
        float textBarY = y - barHeight - 2;
        int teamColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), percentColor);
        graphics.flush();
        //安装提示框
        BlurRectRenderer.render(graphics, textBarX, textBarY, textBarWidth, barHeight, ((int)(alpha * 80 + 1) << 24 | 0x00300000 | 0x00003000 | 0x00000030), alpha * 8.0F);
        //安装提示词
        graphics.drawCenteredString(font, "炸弹安装中", (int) (textBarX + textBarWidth / 2F), (int) (textBarY + (barHeight - font.lineHeight) / 2), percentColor);
        //模糊条
        BlurRectRenderer.render(graphics, x, y, barWidth, barHeight, ((int)(alpha * 80 + 1) << 24 | 0x00300000 | 0x00003000 | 0x00000030), alpha * 8.0F);
        float renderProgress = Math.min(1F, Math.max(0F, progress));
        float progressBarX = x + 2F;
        float progressBarY = y + 3F;
        float progressBarWidth = barWidth - 4F;
        float progressBarHeight = barHeight - 6F;
        float progressWidth = progressBarWidth * renderProgress;
        if (progressWidth > 0F) {
            float whiteProgress = Math.min(1F, Math.max(0F, (renderProgress - 0.8F) / 0.2F));
            int baseR = FastColor.ARGB32.red(0xFF8b9ea8);
            int baseG = FastColor.ARGB32.green(0xFF8b9ea8);
            int baseB = FastColor.ARGB32.blue(0xFF8b9ea8);
            float r = (baseR + (255 - baseR) * whiteProgress) / 255F;
            float g = (baseG + (255 - baseG) * whiteProgress) / 255F;
            float b = (baseB + (255 - baseB) * whiteProgress) / 255F;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.setColor(r, g, b, alpha);
            ModShaders.logoGlitch((player.tickCount + Minecraft.getInstance().getPartialTick()) / 20F, 0.7F + renderProgress * 0.8F);
            graphics.blit(ClientProxy.FPS_UI_ICONS_LOCATION,
                    progressBarX, progressBarY,
                    progressWidth, progressBarHeight,
                    34F, 1F,
                    1F, 1F,
                    512F, 384F,
                    ModShaders::getLogoGlitch
            );
            graphics.setColor(1F, 1F, 1F, 1F);
        }
        //安装提示框左右边框
        graphics.fill(textBarX - 2, textBarY, textBarX, textBarY + barHeight, teamColor);
        graphics.fill(textBarX + textBarWidth, textBarY, textBarX + textBarWidth + 2, textBarY + barHeight, teamColor);
        //模糊条左右边框
        graphics.fill(x - 2, y, x, y + barHeight, teamColor);
        graphics.fill(x + barWidth, y, x + barWidth + 2, y + barHeight, teamColor);
        String percentText = Math.round(renderProgress * 100F) + "%";
        graphics.drawString(font, percentText, progressBarX + progressBarWidth - font.width(percentText) - 1F, progressBarY + (progressBarHeight - font.lineHeight) / 2F, percentColor, true);
        poseStack.popPose();
    }
    public void renderShearingAnimation(LocalPlayer player, ForgeGui gui, MegaGuiGraphics graphics, float progress, int screenWidth, int screenHeight) {
        float alpha = 1F - Easing.IN_OUT_CUBIC.calculate((1F - Math.min(1F, progress * 7F)));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        Font font = gui.getFont();
        int percentColor = ((int)(alpha * 255) << 24) | 0x00A0A0A0;
        float barWidth = screenWidth * 0.3F;
        float barHeight = barWidth * 0.1F;
        float textBarWidth = barWidth * 0.5F;
        float x = (screenWidth - barWidth) / 2;
        float y = screenHeight * 0.75F;
        y += (-10 * (1F - alpha));
        float textBarX = (screenWidth - textBarWidth) / 2;
        float textBarY = y - barHeight - 2;
        int teamColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), percentColor);
        graphics.flush();
        //拆除提示框
        BlurRectRenderer.render(graphics, textBarX, textBarY, textBarWidth, barHeight, ((int)(alpha * 80 + 1) << 24 | 0x00300000 | 0x00003000 | 0x00000030), alpha * 8.0F);
        //拆除提示词
        graphics.drawCenteredString(font, "炸弹拆除中", (int) (textBarX + textBarWidth / 2F), (int) (textBarY + (barHeight - font.lineHeight) / 2), percentColor);
        //模糊条
        BlurRectRenderer.render(graphics, x, y, barWidth, barHeight, ((int)(alpha * 80 + 1) << 24 | 0x00300000 | 0x00003000 | 0x00000030), alpha * 8.0F);
        float renderProgress = Math.min(1F, Math.max(0F, progress));
        float progressBarX = x + 2F;
        float progressBarY = y + 3F;
        float progressBarWidth = barWidth - 4F;
        float progressBarHeight = barHeight - 6F;
        float progressWidth = progressBarWidth * renderProgress;
        if (progressWidth > 0F) {
            float whiteProgress = Math.min(1F, Math.max(0F, (renderProgress - 0.8F) / 0.2F));
            int baseR = FastColor.ARGB32.red(0xFF8b9ea8);
            int baseG = FastColor.ARGB32.green(0xFF8b9ea8);
            int baseB = FastColor.ARGB32.blue(0xFF8b9ea8);
            float r = (baseR + (255 - baseR) * whiteProgress) / 255F;
            float g = (baseG + (255 - baseG) * whiteProgress) / 255F;
            float b = (baseB + (255 - baseB) * whiteProgress) / 255F;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.setColor(r, g, b, alpha);
            ModShaders.logoGlitch((player.tickCount + Minecraft.getInstance().getPartialTick()) / 20F, 0.7F + renderProgress * 0.8F);
            graphics.blit(ClientProxy.FPS_UI_ICONS_LOCATION,
                    progressBarX, progressBarY,
                    progressWidth, progressBarHeight,
                    34F, 1F,
                    1F, 1F,
                    512F, 384F,
                    ModShaders::getLogoGlitch
            );
            graphics.setColor(1F, 1F, 1F, 1F);
        }
        //拆除提示框左右边框
        graphics.fill(textBarX - 2, textBarY, textBarX, textBarY + barHeight, teamColor);
        graphics.fill(textBarX + textBarWidth, textBarY, textBarX + textBarWidth + 2, textBarY + barHeight, teamColor);
        //模糊条左右边框
        graphics.fill(x - 2, y, x, y + barHeight, teamColor);
        graphics.fill(x + barWidth, y, x + barWidth + 2, y + barHeight, teamColor);
        String percentText = Math.round(renderProgress * 100F) + "%";
        graphics.drawString(font, percentText, progressBarX + progressBarWidth - font.width(percentText) - 1F, progressBarY + (progressBarHeight - font.lineHeight) / 2F, percentColor, false);
        poseStack.popPose();
    }
}
