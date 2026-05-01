package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.data.map2.ClientGame1Data;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.mixin.AccessorPlayerTabOverlay;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;

public class TabOverlay implements IGuiOverlay {
    private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.<PlayerInfo>comparingInt((p_253306_) -> p_253306_.getGameMode() == GameType.SPECTATOR ? 1 : 0).thenComparing((p_269613_) -> Optionull.mapOrDefault(p_269613_.getTeam(), PlayerTeam::getName, "")).thenComparing((p_253305_) -> p_253305_.getProfile().getName(), String::compareToIgnoreCase);
    public static int BACKGROUND_DEFAULT_I_COLOR = 0xD8384361;
    public static Vector4f BACKGROUND_DEFAULT_COLOR = new Vector4f(0.019607844F, 0.8666667F, 0.6F, 0.8F);
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (ClientGameData.isStopped) return;
        if (((AccessorPlayerTabOverlay)gui.getTabList()).isVisible()) {
            gui.setupOverlayRenderState(true, false);
            MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
            graphics.bufferSource().endBatch();
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(0, 0, 1000);
            renderBackground(gui, graphics, poseStack, partialTick, screenWidth, screenHeight);
            poseStack.popPose();
        }
    }
    public void renderBackground(ForgeGui gui, MegaGuiGraphics graphics, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        //渲染标题背景
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        String title = getCurrentGameNam();
        Font font = gui.getFont();
        int textUnitWidth = font.width("100");
        float longestWidth = screenWidth * 0.7F;
        float halfLongestWidth = longestWidth / 2F;
        float lineHeight = font.lineHeight * 1.5F;
        float periodOfLine = 2;
        float top = Math.min(60, screenHeight * 0.2F);
        float left = (screenWidth - longestWidth) / 2F;
        float pingX = left + 16;
        float deathsX = left + halfLongestWidth - textUnitWidth;
        float assistsX = deathsX - textUnitWidth * 1.5F;
        float killsX = assistsX - textUnitWidth * 1.5F;
        int barCount = 10;
        RenderSystem.depthFunc(GL11.GL_LESS);
        RenderSystem.enableDepthTest();
        if (!title.isEmpty()) {
            poseStack.translate(0.5F, 0, 0);
            graphics.drawCenteredString(font, title, screenWidth / 2, (int) (top + font.lineHeight * 0.25F), 0xD0505050);
            poseStack.translate(-0.5F, 0, 0);
            graphics.drawCenteredString(font, title, screenWidth / 2, (int) (top + font.lineHeight * 0.25F), 0xFFFFFFFF);
            graphics.fill(left, top, left + longestWidth, top + lineHeight, BACKGROUND_DEFAULT_I_COLOR);
            top += lineHeight;
        }//渲染列名条
        graphics.fill(left, top, left + longestWidth, top + lineHeight, BACKGROUND_DEFAULT_I_COLOR);
        //渲染列名
        for (int l=0;l<=1;l++) {
            poseStack.translate(l * halfLongestWidth, 2, 0);
            renderPingIcon(graphics, pingX, top);
            float nameX = textUnitWidth + 2 + graphics.drawString(font, "延迟", Mth.ceil(pingX + 6), (int) (top), 0xFFA0A0A0);
            graphics.drawCenteredString(font, "昵称", (int) nameX, (int) top, 0xFFA0A0A0);
            graphics.drawCenteredString(font, "击杀", (int) killsX, (int) top, 0xFFA0A0A0);
            graphics.drawCenteredString(font, "助攻", (int) assistsX, (int) top, 0xFFA0A0A0);
            graphics.drawCenteredString(font, "死亡", (int) deathsX, (int) top, 0xFFA0A0A0);
            poseStack.translate(-l * halfLongestWidth, -2, 0);
        }
        top += lineHeight + periodOfLine;
        //1~20人大小框
        float realHudHeight = top + (lineHeight + periodOfLine) * barCount + periodOfLine;
        graphics.fill(left, top - periodOfLine, left + halfLongestWidth - 1, realHudHeight, 0xD8303030);
        graphics.fill(left + halfLongestWidth + 1, top - periodOfLine, left + longestWidth, realHudHeight, 0xD8303030);
        graphics.fill(left + halfLongestWidth - 1, top - periodOfLine, left + halfLongestWidth + 1, realHudHeight, BACKGROUND_DEFAULT_I_COLOR);

        //渲染每条的背景
        for (int i=0;i<barCount;i++) {
            float yOffset = (lineHeight + periodOfLine) * i;
            graphics.fill(killsX - textUnitWidth, top + yOffset, left + halfLongestWidth - 6, top + yOffset + lineHeight, 0x70100010);
            graphics.fill(killsX - textUnitWidth + halfLongestWidth, top + yOffset, left + halfLongestWidth - 6 + halfLongestWidth, top + yOffset + lineHeight, 0x70100010);
        }
        if (ClientGameData.isTeamMode) {
            List<PlayerInfo> playerInfos = getPlayerInfos(player);
            //渲染蓝队
            int yLeftOffset = 0;
            int yRightOffset = 0;
            ChatFormatting localPColor = player.getTeam() == null ? ChatFormatting.WHITE : player.getTeam().getColor();
            for (PlayerInfo playerInfo : playerInfos) {
                if (playerInfo.getProfile().getName().equals(player.getGameProfile().getName())
                                || (playerInfo.getTeam() != null && playerInfo.getTeam().getColor() == localPColor)) {
                    renderSinglePlayerInfo(graphics, font, left, top + yLeftOffset, halfLongestWidth, playerInfo);
                    yLeftOffset += lineHeight + periodOfLine;
                } else {
                    renderSinglePlayerInfo(graphics, font, left + halfLongestWidth, top + yRightOffset, halfLongestWidth, playerInfo);
                    yRightOffset += lineHeight + periodOfLine;
                }
            }
        } else {
            List<PlayerInfo> playerInfos = getPlayerInfosSortKAD(player);
            int yOffset = 0;
            for (int index = 0;index<playerInfos.size();index++) {
                PlayerInfo playerInfo = playerInfos.get(index);
                float offset = (index + 1) % 2 == 0 ? halfLongestWidth : 0;
                renderSinglePlayerInfo(graphics, font, left + offset, top + yOffset, halfLongestWidth, playerInfo);
                if (offset > 0)
                    yOffset += lineHeight + periodOfLine;
            }
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }
    public static String getCurrentGameNam() {
        String v = "";
        if (!ClientGameData.isStopped) {
            if (!ClientGame1Data.isStopped) {
                v = "武器大乱斗" + (ClientGameData.isTeamMode ? "-团队战" : "个人战");
            } else if (!ClientGame2Data.isStopped) {
                v = "幽灵模式";
            }
        }
        return v;
    }
    private List<PlayerInfo> getPlayerInfos(LocalPlayer player) {
        return player.connection.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
    }
    private List<PlayerInfo> getPlayerInfosSortKAD(LocalPlayer player) {
        return player.connection.getListedOnlinePlayers().stream().sorted(ClientFpsData.PLAYER_COMPARATOR).limit(80L).toList();
    }
    private void renderSinglePlayerInfo(MegaGuiGraphics graphics, Font font, float x, float y, float width, PlayerInfo playerInfo) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 2, 0);
        float rawX = x;
        x += 24;
        int textUnitWidth = font.width("100");
        //渲染延迟数字
        String latencyS = String.valueOf(playerInfo.getLatency());
        if (playerInfo.getLatency() < 0) {
            graphics.drawCenteredString(font, Component.literal("离线").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), (int) (x), (int) y, 0xFFFFFFFF);
        } else if (playerInfo.getLatency() < 150) {
            graphics.drawCenteredString(font, Component.literal(latencyS).withStyle(ChatFormatting.GREEN), (int) (x), (int) y, 0xFFFFFFFF);
        } else if (playerInfo.getLatency() < 300) {
            graphics.drawCenteredString(font, Component.literal(latencyS).withStyle(ChatFormatting.YELLOW), (int) (x), (int) y, 0xFFFFFFFF);
        } else if (playerInfo.getLatency() < 600) {
            graphics.drawCenteredString(font, Component.literal(latencyS).withStyle(ChatFormatting.RED), (int) (x), (int) y, 0xFFFFFFFF);
        } else {
            graphics.drawCenteredString(font, Component.literal(latencyS).withStyle(ChatFormatting.DARK_RED), (int) (x), (int) y, 0xFFFFFFFF);
        }
        //渲染名字
        boolean isDead = ClientFpsData.getPlayerTabDead(playerInfo);
        float headScale = font.lineHeight * 1.1F;
        SelectPlayerOverlay.renderProfileIcon(playerInfo.getSkinLocation(), graphics, x + 19 + headScale/2F, y - 1 + headScale/2F, headScale, isDead);
        drawPlayerInfoName(
                graphics, font,
                isDead ? Component.literal("").append(ClientFpsData.getPlayerName(playerInfo)).withStyle(ChatFormatting.STRIKETHROUGH) : ClientFpsData.getPlayerName(playerInfo),
                (int) (x + 20 + font.lineHeight + 1), (int) y, (int) (width - textUnitWidth * 5 - 20 - 24 - font.width(latencyS) - font.lineHeight), 0xFFFFFFFF
        );
        //渲染KAD
        float deathsX = rawX + width - textUnitWidth;
        KAD kad = ClientFpsData.getPlayerKAD(playerInfo).getOrDefaultKAD(KAD.KAD_GENERAL);
        poseStack.translate(deathsX, y, 0);
        graphics.drawCenteredString(font, String.valueOf(kad.deaths), 0, 0, 0xFFD0D0D0);
        poseStack.translate(- textUnitWidth * 1.5F, 0, 0);
        graphics.drawCenteredString(font, String.valueOf(kad.assists), 0, 0, 0xFFD0D0D0);
        poseStack.translate(- textUnitWidth * 1.5F, 0, 0);
        graphics.drawCenteredString(font, String.valueOf(kad.kills), 0, 0, 0xFFD0D0D0);
        poseStack.popPose();
    }
    protected void renderPingIcon(MegaGuiGraphics guiGraphics, float x, float y, PlayerInfo playerInfo) {
        int j;
        if (playerInfo.getLatency() < 0) {
            j = 5;
        } else if (playerInfo.getLatency() < 150) {
            j = 0;
        } else if (playerInfo.getLatency() < 300) {
            j = 1;
        } else if (playerInfo.getLatency() < 600) {
            j = 2;
        } else if (playerInfo.getLatency() < 1000) {
            j = 3;
        } else {
            j = 4;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        guiGraphics.blit(new ResourceLocation("textures/gui/icons.png"), x - 5, y, 0, 176 + j * 8, 10, 8);
        guiGraphics.pose().popPose();
    }
    protected void renderPingIcon(MegaGuiGraphics guiGraphics, float x, float y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        guiGraphics.blit(new ResourceLocation("textures/gui/icons.png"), x - 5, y, 0, 176, 10, 8);
        guiGraphics.pose().popPose();
    }
    public void drawPlayerInfoName(GuiGraphics graphics, Font font, FormattedText text, int x, int y, int maxLength, int color) {
        for(FormattedCharSequence formattedcharsequence : font.split(text, maxLength)) {
            graphics.drawString(font, formattedcharsequence, x, y, color);
            break;
        }
    }
}
