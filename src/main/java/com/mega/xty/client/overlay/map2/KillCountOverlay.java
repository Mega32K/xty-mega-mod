package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.client.screen.BlitInfo;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.shader.ModShaders;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.map2.ClientGame1Data;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.proxy.ClientProxy;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.lwjgl.opengl.GL11;

public class KillCountOverlay implements IGuiOverlay {
    public static final BlitInfo KILLCOUNT = new BlitInfo(
            ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/display_killcount.png"),
            0, 0, 128, 17
    );
    public static final BlitInfo TEAM_SCORE = new BlitInfo(
            ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/display_killcount.png"),
            0, 17, 35, 16
    );
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
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui || !ClientGameData.scoreOverlayVisible) return;
        if (!ClientGameData.map2Playing()) return;
        float game1OverlayOffset = 0F;
        Minecraft mc = gui.getMinecraft();
        int redColor = ChatFormatting.RED.getColor();
        int blueColor = ChatFormatting.BLUE.getColor();
        gui.setupOverlayRenderState(true, false);
        MegaGuiGraphics graphics = new MegaGuiGraphics(gui.getMinecraft(), guiGraphics.bufferSource());
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        //偏移中心
        poseStack.translate(graphics.guiWidth() / 2F , 24.5, 0);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        RenderSystem.depthFunc(GL11.GL_LESS);
        RenderSystem.enableDepthTest();
        graphics.setColor(0, 0, 0, 0.5F);
        for (int i=-1;i<=1;i++) {
            for (int j=-1;j<=1;j++) {
                graphics.blit(KILLCOUNT.texture(), -KILLCOUNT.width() / 2F + i, -KILLCOUNT.height() + j, 0F, 0F, 128F, 17F);
            }
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        graphics.setColor(1F, 1F, 1F, 1F);
        graphics.blit(KILLCOUNT.texture(), -KILLCOUNT.width() / 2F, -KILLCOUNT.height(), KILLCOUNT.startX(), KILLCOUNT.startY(), KILLCOUNT.endX(), KILLCOUNT.endY(), ModShaders::getMapPositionTex);
        poseStack.popPose();

        game1OverlayOffset = 24.5F + KILLCOUNT.height() + 5F;

        //绘制文本
        Player player = gui.getMinecraft().player;
        if (!ClientGameData.isTeamMode) {
            if (player != null) {
                CommonProxy.getMap2Cap(player).ifPresent(map2Capability -> {
                    Font font = gui.getFont();
                    MutableComponent left = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal("杀敌: ").withStyle(ChatFormatting.RED))
                            .append(Component.literal("%s".formatted(map2Capability.get1KillCount())).withStyle(ChatFormatting.GRAY));
                    MutableComponent mid = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(ClientGame1Data.formatTime()).withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));
                    MutableComponent right = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal("1.ST: ").withStyle(style -> style.withColor(0x649ee8)))
                            .append(Component.literal("%s".formatted(map2Capability.get2KillCount())).withStyle(ChatFormatting.GRAY));
                    int width1 = font.width(left);
                    int width2 = font.width(mid);
                    int width3 = font.width(right);
                    graphics.drawCenteredString(
                            font,
                            mid,
                            graphics.guiWidth() / 2, 12, 0xFFFFFFFF);
                    graphics.drawCenteredString(
                            font,
                            left,
                            graphics.guiWidth() / 2 - Mth.ceil(width1 / 2F + width2 / 2F), 12, 0xFFFFFFFF);
                    graphics.drawCenteredString(
                            font,
                            right,
                            graphics.guiWidth() / 2 + Mth.ceil(width2 / 2F + width3 / 2F), 12, 0xFFFFFFFF);
                });
            }
        } else {
            if (player != null) {
                Font font = gui.getFont();
                MutableComponent left = null;MutableComponent mid = null;

                MutableComponent right = null;
                if (ClientGame1Data.playing()) {
                    right = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal("保卫者: ").withStyle(style -> style.withColor(0x649ee8)))
                            .append(Component.literal("%s".formatted(ClientGameData.blueTeamKillcount)).withStyle(ChatFormatting.GRAY));
                    mid = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(ClientGame1Data.formatTime()).withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));
                    left = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal("幽灵: ").withStyle(ChatFormatting.RED))
                            .append(Component.literal("%s".formatted(ClientGameData.redTeamKillcount)).withStyle(ChatFormatting.GRAY));
                } else if (ClientGame2Data.playing()) {
                    right = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal("保卫者 ").withStyle(style -> style.withColor(0x649ee8)));
                    mid = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(ClientGame1Data.formatTime()).withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));
                    left = Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(" 幽灵").withStyle(ChatFormatting.RED));
                }

                    int width1 = font.width(left);
                int width2 = font.width(mid);
                int width3 = font.width(right);
                if (player.getTeamColor() == blueColor) {
                    int v = width1;
                    width1 = width3;
                    width3 = v;
                    MutableComponent v2 = left;
                    left = right;
                    right = v2;
                }
                if (!ClientFpsData.bombExist) {
                    graphics.drawCenteredString(
                            font,
                            mid,
                            graphics.guiWidth() / 2, 12, 0xFFFFFFFF);
                } else {
                    float size = 18F;
                    //渲染C4图标
                    graphics.setColor(0x88 / 255F, 0F, 0x1B / 255F, 1F);
                    graphics.blit(C4_2.texture(),
                            screenWidth / 2F - size / 2F + 2, 6.5F,
                            size, size,
                            C4_2.startX(), C4_2.startY(),
                            C4_2.width(), C4_2.height(),
                            512F, 384F);
                    float[] c4Color = getC4IconColor(partialTick);
                    graphics.setColor(c4Color[0], c4Color[1], c4Color[2], c4Color[3]);
                    graphics.blit(C4_1.texture(),
                            screenWidth / 2F - size / 2F + 2, 6.5F,
                            size, size,
                            C4_1.startX(), C4_1.startY(),
                            C4_1.width(), C4_1.height(),
                            512F, 384F);
                    graphics.setColor(1F, 1F, 1F, 1F);
                }
                graphics.drawCenteredString(
                        font,
                        left,
                        graphics.guiWidth() / 2 - Mth.ceil(width1 / 2F + width2 / 2F), 12, 0xFFFFFFFF);
                graphics.drawCenteredString(
                        font,
                        right,
                        graphics.guiWidth() / 2 + Mth.ceil(width2 / 2F + width3 / 2F), 12, 0xFFFFFFFF);
                //渲染队伍得分
                if (ClientGameData.teamScoreVisible) {
                    int yOffset = 30 + KILLCOUNT.height();
                    poseStack.pushPose();
                    //偏移中心
                    poseStack.translate(graphics.guiWidth() / 2F , yOffset, 0);
                    poseStack.scale(1.25F, 1.25F, 1.25F);
                    graphics.blit(TEAM_SCORE.texture(), -TEAM_SCORE.width() / 2F, -TEAM_SCORE.height(), TEAM_SCORE.startX(), TEAM_SCORE.startY(), TEAM_SCORE.endX(), TEAM_SCORE.endY(), ModShaders::getMapPositionTex);
                    poseStack.popPose();
                    //渲染队伍分
                    int leftScore = ClientGameData.redTeamScore;
                    int rightScore = ClientGameData.blueTeamScore;
                    int leftColor = 0xddFF5555;
                    int rightColor = 0xdd649ee8;
                    if (player.getTeamColor() == blueColor) {
                        int v = leftScore;
                        leftScore = rightScore;
                        rightScore = v;
                        int v2 = leftColor;
                        leftColor = rightColor;
                        rightColor = v2;
                    }
                    String leftScoreS = String.valueOf(leftScore);
                    String rightScoreS = String.valueOf(rightScore);
                    graphics.drawCenteredString(
                            font,
                            Component.literal(leftScoreS),
                            (int) (graphics.guiWidth() / 2F) - font.width(leftScoreS + " :"),
                            yOffset - 15,
                            leftColor
                    );
                    graphics.drawCenteredString(
                            font,
                            Component.literal(rightScoreS),
                            (int) (graphics.guiWidth() / 2F) + font.width(rightScoreS + ": "),
                            yOffset - 15,
                            rightColor
                    );
                    poseStack.pushPose();
                    poseStack.translate((int) (graphics.guiWidth() / 2F), yOffset - 15, 0);
                    poseStack.scale(0.6F, 0.6F, 0.6F);
                    graphics.drawCenteredString(
                            font,
                            "比",
                            0,
                            -3,
                            0xFFEF8881
                    );
                    graphics.drawCenteredString(
                            font,
                            "分",
                            0,
                            6,
                            0xFFEF8881
                    );
                    poseStack.popPose();
                    game1OverlayOffset += TEAM_SCORE.height();
                }
                //渲染两队玩家
                if (mc.level != null) {
                    poseStack.pushPose();
                    //偏移中心
                    poseStack.translate(graphics.guiWidth() / 2F , 17, 0);
                    int headSize = 8;
                    float leftOffset = 0F;
                    float rightOffset = 0F;
                    float m = player.getTeamColor() == blueColor ? 1F : -1F;
                    for (AbstractClientPlayer p : mc.level.players()) {
                        if (p.getTeamColor() == blueColor) {
                            renderPerson(p, graphics, poseStack, headSize, leftOffset - 95F * m, 0xCC3a5595, 0x103a5595);
                            leftOffset -= headSize * 2.625F * m;
                        } else if (p.getTeamColor() == redColor) {
                            renderPerson(p, graphics, poseStack, headSize, rightOffset + 95F * m, 0xCCFF0000, 0x10FF0000);
                            rightOffset += headSize * 2.625F * m;
                        }
                    }
                    poseStack.popPose();
                }
            }
        }
        new Game1Overlay().render2(gui, guiGraphics, partialTick, screenWidth, screenHeight, game1OverlayOffset);
        RenderSystem.disableBlend();
    }
    private static void renderPerson(AbstractClientPlayer player, MegaGuiGraphics graphics, PoseStack poseStack, int headSize, float offset, int color1, int color2) {
        poseStack.pushPose();
        poseStack.translate(offset, 0, 0);
        graphics.fillGradient(-2 - headSize, -5 - headSize, headSize + 2, headSize, color1, color2);
        float progress = player.getHealth() /  player.getMaxHealth();

        Player localP = ClientWrapped.clientPlayer();
        if (localP == null || localP.getTeam() == null || localP.getTeam().isAlliedTo(player.getTeam())) {
            CommonProxy.getMap2Cap(player).ifPresent(cap -> {
                if (!cap.isXaeroDead()) graphics.fill(-2-headSize, headSize+1, Mth.lerp(progress, -2-headSize, headSize + 2), headSize + 4, 0xFFFFFFFF);
            });
        }
        HealthOverlay.renderProfileIcon(player, graphics, poseStack, headSize);
        poseStack.popPose();
    }
    private static float[] getC4IconColor(float partialTick) {
        float baseR = 0x88 / 255F;
        float baseB = 0x1B / 255F;
        float flash = getC4IconFlashStrength(partialTick);
        float alpha = getC4IconAlpha(partialTick);
        return new float[] {
                baseR + (1.0F - baseR) * flash,
                0.0F,
                baseB * (1.0F - flash),
                alpha
        };
    }
    private static float getC4IconAlpha(float partialTick) {
        int interval = ClientFpsData.getBombBeepIntervalTicks(ClientFpsData.bombCountdownTicks);
        int ticksSinceBeep = ((interval - ((ClientFpsData.bombCountdownTicks + 1) % interval)) % interval);
        float flashDuration = interval > 10 ? 6.0F : interval > 5 ? 4.0F : 2.0F;
        float progress = Math.min(1.0F, (ticksSinceBeep + partialTick) / flashDuration);
        return 1.0F - Easing.IN_OUT_SINE.calculate(progress);
    }
    private static float getC4IconFlashStrength(float partialTick) {
        int interval = ClientFpsData.getBombBeepIntervalTicks(ClientFpsData.bombCountdownTicks);
        int ticksSinceBeep = ((interval - ((ClientFpsData.bombCountdownTicks + 1) % interval)) % interval);
        float flashDuration = interval > 10 ? 6.0F : interval > 5 ? 4.0F : 2.0F;
        float progress = Math.min(1.0F, (ticksSinceBeep + partialTick) / flashDuration);
        return 1.0F - Easing.OUT_CUBIC.calculate(progress);
    }
}
