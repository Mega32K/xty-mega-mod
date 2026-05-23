package com.mega.map.client.overlay.map2;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.map.client.font.ErrorFont;
import com.mega.map.client.renderer.SafeBlurRectRenderer;
import com.mega.map.client.shader.ModShaders;
import com.mega.map.common.data.fps.ClientFpsData;
import com.mega.map.common.data.fps.kad.KAD;
import com.mega.map.proxy.ClientProxy;
import com.mega.map.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix4f;

import java.util.Optional;

public class LoseOverlay implements IGuiOverlay {
    private static final float MVP_WIDTH_SCALE = 1.25F;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (!ClientFpsData.shouldRenderRoundLose()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        boolean showResult = CommonProxy.getFPSCap(player).map(cap -> cap.getGame2ClientOptions().hud().showRoundResultOverlay()).orElse(true);
        if (!showResult) return;
        boolean showMvp = CommonProxy.getFPSCap(player).map(cap -> cap.getGame2ClientOptions().hud().showRoundMvpOverlay()).orElse(true);

        gui.setupOverlayRenderState(true, false);
        MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
        float titleAlpha = ClientFpsData.getRoundLoseNotificationAlpha(partialTick);
        if (titleAlpha <= 0.0F) return;
        float mvpAlpha = ClientFpsData.getRoundLoseMvpNotificationAlpha(partialTick);

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

        renderTitleBox(graphics, font, "回合失败", x, titleY, width, titleHeight, titleAlpha, titleTextColor, titleBorderColor);
        if (showMvp && mvpAlpha > 0.0F) {
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
        if (realWidth < width) {
            graphics.drawString(ErrorFont.INSTANCE, text, 0, 0, textColor, true);
        } else {
            graphics.drawString(font, text, 0, 0, textColor, true);
        }
        poseStack.popPose();
        graphics.disableScissor();
        renderWhiteFlash(graphics, centerX, y, realWidth, height, alpha);
    }

    private static void renderMvpBox(MegaGuiGraphics graphics, Font font, PlayerInfo playerInfo, float centerX, float y, float width, float height, float alpha, int textColor, int borderColor) {
        float realWidth = alpha * width;
        renderNotificationBackground(graphics, centerX, y, realWidth, height, alpha, borderColor);
        renderMvpBackground(graphics, centerX, y, realWidth, height, alpha, borderColor);
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
        SafeBlurRectRenderer.render(graphics, centerX - realWidth / 2.0F, y, realWidth, height, ((int)(alpha * 80.0F + 1.0F) << 24) | 0x00303030, alpha * 8.0F);
        graphics.fill(centerX - 2.0F - realWidth / 2.0F, y, centerX - realWidth / 2.0F, y + height, borderColor);
        graphics.fill(centerX + realWidth / 2.0F, y, centerX + realWidth / 2.0F + 2.0F, y + height, borderColor);
    }
    private static void renderMvpBackground(MegaGuiGraphics graphics, float centerX, float y, float realWidth, float height, float alpha, int borderColor) {
        final float edgeDarkScale = 0.6F;
        ShaderInstance s = ModShaders.getVoronoiFlowBackground();
        graphics.flush();
        float red = FastColor.ARGB32.red(borderColor) / 255.0F;
        float green = FastColor.ARGB32.green(borderColor) / 255.0F;
        float blue = FastColor.ARGB32.blue(borderColor) / 255.0F;
        ModShaders.voronoiFlowColorA(red * edgeDarkScale, green * edgeDarkScale, blue * edgeDarkScale, 1F, s);
        ModShaders.voronoiFlowColorB(red, green, blue, 1F, s);
        ModShaders.voronoiFlowColorC(red * edgeDarkScale, green * edgeDarkScale, blue * edgeDarkScale, 1F, s);
        ModShaders.voronoiFlowInit(graphics.guiWidth() * 1.25F, graphics.guiHeight() * 1.15F, s);
        RenderSystem.setShaderTexture(0, ClientProxy.WHITE);
        RenderSystem.setShader(ModShaders::getVoronoiFlowBackground);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, centerX - realWidth / 2.0F, y, 0).uv(0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, centerX - realWidth / 2.0F, y + height, 0).uv(0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, centerX + realWidth / 2.0F, y + height, 0).uv(1, 1).endVertex();
        bufferbuilder.vertex(matrix4f, centerX + realWidth / 2.0F, y, 0).uv(1, 0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        graphics.fill(centerX - 2.0F - realWidth / 2.0F, y, centerX - realWidth / 2.0F, y + height, borderColor);
        graphics.fill(centerX + realWidth / 2.0F, y, centerX + realWidth / 2.0F + 2.0F, y + height, borderColor);
    }
    private static void renderWhiteFlash(MegaGuiGraphics graphics, float centerX, float y, float realWidth, float height, float alpha) {
        if (ClientFpsData.roundLoseRenderTimer >= ClientFpsData.ROUND_LOSE_PROMPT_DURATION - 10) {
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
                .filter(ClientFpsData::isGame2MvpCandidate)
                .max(ClientFpsData.PLAYER_COMPARATOR);
    }
}
