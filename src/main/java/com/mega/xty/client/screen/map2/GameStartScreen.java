package com.mega.xty.client.screen.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.client.screen.BlitInfo;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.mixin.accessor.AccessorGuiGraphics;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.endinglib.util.time.TimeContext;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.shader.ModShaders;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicBoolean;

public class GameStartScreen extends Screen {
    public static final ResourceLocation TITLE = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/gun_mini_game.png");
    public static final BlitInfo TITLE_BLIT = new BlitInfo(
            TITLE,
            0, 0, 2388, 256
    );
    public static final BlitInfo CREATOR_0 = new BlitInfo(
            ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/mega.png"),
            0, 0, 144, 144
    );
    public static final BlitInfo CREATOR_1 = new BlitInfo(
            ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/xiaowu.png"),
            0, 0, 155, 155
    );
    public int startInterpolationTick;
    public int tickCount;
    public int waitingForClosing = Integer.MAX_VALUE;
    public int currentPlayerCount;
    public boolean canUpdateProgressBar = false;
    final int creatorIconStart = 40;
    final int creatorIconStop = 120;
    final int creatorIconInOutDuration = 15;
    final int creatorIconNormalDuration = creatorIconStop - creatorIconStart - creatorIconInOutDuration * 2;
    public GameStartScreen(Component title) {
        super(title);
    }
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = TimeContext.safeClientFrameTime();
        super.render(graphics, mouseX, mouseY, partialTicks);
        MegaGuiGraphics guiGraphics = MegaGuiGraphics.of(graphics);
        int guiWidth = guiGraphics.guiWidth();
        int guiHeight = guiGraphics.guiHeight();
        PoseStack poseStack = graphics.pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        {
            float backgroundAlpha = Math.min(1.0F, (this.tickCount + partialTicks) / 20F);
            graphics.setColor(1F, 1F, 1F, backgroundAlpha);
            graphics.fill(-1, -1, guiWidth + 1, guiHeight + 1, 0xFF676767);
            graphics.setColor(1F, 1F, 1F, 1F);
        }
        if (this.tickCount < creatorIconStop) {
            if (this.tickCount > creatorIconStart) {
                int creatorTick = this.tickCount - creatorIconStart;
                float creatorIconsAlpha =  creatorTick < creatorIconInOutDuration
                        ? (creatorTick + partialTicks) / creatorIconInOutDuration
                        : (creatorTick < creatorIconInOutDuration + creatorIconNormalDuration
                            ? 1F
                            : 1F - (creatorTick - creatorIconInOutDuration - creatorIconNormalDuration + partialTicks) / creatorIconInOutDuration
                    );
                creatorIconsAlpha = Easing.IN_OUT_CUBIC.calculate(creatorIconsAlpha);
                renderCreators(guiGraphics, poseStack, guiWidth / 2, guiHeight / 2, creatorIconsAlpha);
            }
        } else {
            this.tickCount -= creatorIconStop;
            float backgroundAlpha = Math.min(1.0F, (this.tickCount + partialTicks) / 20F);
            //[-∞, 2F]
            float progress = 2F - (this.tickCount + partialTicks) / 40F;
            graphics.setColor(1F, 1F, 1F, backgroundAlpha);
            graphics.fill(-1, -1, guiWidth + 1, guiHeight + 1, 0xFF1b2238);
            float width = guiWidth / 4F;
            float ratio = TITLE_BLIT.height() / (float) TITLE_BLIT.width();
            ModShaders.dissolve2d(progress);
            guiGraphics.blit(TITLE,
                    guiWidth / 2F - width / 2F, guiHeight / 2F - 40,
                    width, width * ratio,
                    0, 0,
                    TITLE_BLIT.width(), TITLE_BLIT.height(),
                    TITLE_BLIT.width(), TITLE_BLIT.height(),
                    ModShaders::getDissolve2d);
            //进度条
            {
                //标题已开始渲染
                canUpdateProgressBar = progress < 1F;
                float barAlpha;
                if (!canUpdateProgressBar) {
                    barAlpha = 0F;
                } else if (waitingForClosing > 10000) {
                    barAlpha = Math.min(1.0F, progress * -1F + 1F);
                    if (barAlpha >= 1.0F) {
                        if (currentPlayerCount >= ClientGameData.playerCountNeed) {
                            waitingForClosing = 160;
                        }
                    }
                } else {
                    barAlpha = Math.min(1.0F, (waitingForClosing - partialTicks) / 80F);
                    backgroundAlpha *= barAlpha;
                }
                poseStack.pushPose();
                poseStack.translate(0, 0, 10);
                float d1 = Math.min(guiWidth * 0.75F, guiHeight) * 0.25F;
                int yOffset = (int) (guiHeight * 0.5F);
                float d0 = d1 * 4.0F;
                int j1 = (int) (d0 * 0.5D);
                drawProgressBar(graphics, guiWidth / 2 - j1, yOffset - 5, guiWidth / 2 + j1, yOffset + 5, barAlpha, partialTicks);
                //下方渲染加载人数文本
                graphics.setColor(1F, 1F, 1F, barAlpha);
                String loadingS = currentPlayerCount >= ClientGameData.playerCountNeed ? "加载完毕" : "等待玩家中...";
                String playersS = "(" + currentPlayerCount + "/" + ClientGameData.playerCountNeed + ")";
                graphics.drawCenteredString(font, loadingS, guiWidth / 2, yOffset + font.lineHeight * 2, 0xFFFFFFFF);
                //因为缩小所以要用posestack居中
                poseStack.pushPose();
                poseStack.translate(guiWidth / 2F, yOffset + font.lineHeight * 3, 0);
                poseStack.scale(0.75F, 0.75F, 0.75F);
                graphics.drawCenteredString(font, playersS, 0, 0, 0xFFFFFFFF);
                poseStack.popPose();
                poseStack.popPose();

                graphics.setColor(1F, 1F, 1F, 1F);
            }
            //名单
            graphics.setColor(1F, 1F, 1F, backgroundAlpha);

            poseStack.pushPose();
            float scale = 0.75F;
            float lineHeight = font.lineHeight * scale;
            poseStack.translate(guiWidth / 2F, guiHeight - 3 - lineHeight, 0);
            poseStack.scale(scale, scale, scale);
            graphics.drawCenteredString(font, "Powered by Mega, XiaoWu", 0, 0, 0xFFa1a1a1);
            poseStack.popPose();
            graphics.setColor(1F, 1F, 1F, 1F);
            this.tickCount += creatorIconStop;
        }
        RenderSystem.disableBlend();
    }
    private void renderCreators(MegaGuiGraphics graphics, PoseStack poseStack, int x, int y, float alpha) {
        float defaultScale = Math.max(graphics.guiWidth(), graphics.guiHeight()) * 0.02F;
        float iconSize = defaultScale * 8F;
        graphics.setColor(1F, 1F, 1F, alpha);
        //渲染乘号
        connectRender(graphics, poseStack, x, y, defaultScale);
        //渲染icon
        ModShaders.rgbOutline(1F, 1F, 0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(CREATOR_0.texture(),
                x - iconSize - iconSize / 2F, y - iconSize / 2F,
                iconSize, iconSize,
                CREATOR_0.startX(), CREATOR_0.startY(),
                CREATOR_0.endX(), CREATOR_0.endY(),
                CREATOR_0.width(), CREATOR_0.height(),
                ModShaders::getRgbOutline
                );
        graphics.blit(CREATOR_1.texture(),
                x + iconSize - iconSize / 2F, y - iconSize / 2F,
                iconSize, iconSize,
                CREATOR_1.startX(), CREATOR_1.startY(),
                CREATOR_1.endX(), CREATOR_1.endY(),
                CREATOR_1.width(), CREATOR_1.height()
        );
        RenderSystem.disableBlend();
        graphics.setColor(1F, 1F, 1F, 1F);
        RenderSystem.disableDepthTest();
    }
    private void connectRender(MegaGuiGraphics graphics, PoseStack poseStack, int x, int y, float defaultScale) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(45F));
        renderPlusSign(graphics, defaultScale, poseStack.last().pose());
        poseStack.popPose();
    }
    private void renderPlusSign(MegaGuiGraphics graphics, float size, Matrix4f matrix4f) {
        VertexConsumer vertexConsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        //横
        vertexConsumer
                .vertex(matrix4f, -size * 1.5F, -size * 0.5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, -size * 1.5F, size * 0.5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, size * 1.5F, size * 0.5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, size * 1.5F, -size * 0.5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        //竖
        vertexConsumer
                .vertex(matrix4f, -size * 0.5F, -size * 1.5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, -size * 0.5F, -size * .5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, size * 0.5F, -size * .5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, size * 0.5F, -size * 1.5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();

        vertexConsumer
                .vertex(matrix4f, -size * 0.5F, size * .5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, -size * 0.5F, size * 1.5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, size * 0.5F, size * 1.5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, size * 0.5F, size * .5F, 0)
                .color(255, 255, 255, 255)
                .endVertex();
        graphics.flush();
    }
    private void drawProgressBar(GuiGraphics graphics, int x, int y, int endX, int endY, float alpha, float partialTicks) {
        int i = Mth.ceil((float)(endX - x - 2) * (calculateInterpolationProgress(partialTicks) * Math.clamp((float) currentPlayerCount / ClientGameData.playerCountNeed, 0, 1)));
        int j = Math.round(alpha * 255.0F);
        int k = FastColor.ARGB32.color(j, 255, 255, 255);
        graphics.fill(x + 2, y + 2, x + i, endY - 2, k);
        graphics.fill(x + 1, y, endX - 1, y + 1, k);
        graphics.fill(x + 1, endY, endX - 1, endY - 1, k);
        graphics.fill(x, y, x + 1, endY, k);
        graphics.fill(endX, y, endX - 1, endY, k);
    }
    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (canUpdateProgressBar) {
            if (mc.level != null) {
                int lastCount = currentPlayerCount;
                currentPlayerCount = mc.level.players().size();
                if (currentPlayerCount != lastCount) {
                    this.startInterpolationTick = tickCount - creatorIconStop;
                }
            }
        }
        tickCount++;
        waitingForClosing--;
        super.tick();
        if (waitingForClosing <= 0) {
            if (ClientWrapped.clientPlayer() instanceof AbstractClientPlayer cp)
                mc.execute(()-> {
                    mc.forceSetScreen(new RenameScreen(cp));
                });
        }
    }
    public float calculateInterpolationProgress(float partialTicks) {
        int i = 15;
        float f = (float)(tickCount - this.startInterpolationTick);
        float f1 = f + partialTicks;
        return Mth.clamp(Mth.inverseLerp(f1, 0.0F, (float)i), 0.0F, 1.0F);
    }
}
