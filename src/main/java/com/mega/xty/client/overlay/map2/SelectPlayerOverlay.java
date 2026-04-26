package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.api.client.cmc.LoreHelper;
import com.mega.endinglib.api.client.screen.BlitInfo;
import com.mega.endinglib.mixin.accessor.AccessorGuiGraphics;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.shader.post.map2.DeathCameraEffectHandler;
import com.mega.xty.client.shader.ModShaders;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.proxy.ClientProxy;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;

import java.util.Optional;

public class SelectPlayerOverlay implements IGuiOverlay {
    public static final int FPS_UI_RX = 512;
    public static final int FPS_UI_RY = 384;
    public static final float HEAD_LAYER_SCALE = 1.0625F;
    public static final BlitInfo ARMOR = new BlitInfo(
            ClientProxy.FPS_UI_ICONS_LOCATION,
            0, 0,
            128, 128
    );
    public static final BlitInfo HEART = new BlitInfo(
            ClientProxy.FPS_UI_ICONS_LOCATION,
            0, 128,
            128, 128
    );
    static float textScale = 0.75F;
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (ClientGameData.isStopped) return;
        if (DeathCameraEffectHandler.isPlaying()) return;
        gui.setupOverlayRenderState(true, false);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            CommonProxy.getMap2Cap(mc.player).ifPresent(cap -> {
                if (cap.isXaeroDead()) {
                    if (mc.getCameraEntity() instanceof AbstractClientPlayer clientPlayer)
                        renderCameraEntityInfo(gui, MegaGuiGraphics.of(guiGraphics), partialTick, screenWidth, screenHeight, mc.player, clientPlayer);
                }
            });
        }
    }
    public void renderCameraEntityInfo(ForgeGui gui, MegaGuiGraphics graphics, float partialTick, int screenWidth, int screenHeight, LocalPlayer player, AbstractClientPlayer cameraP) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        Font font = gui.getFont();
        graphics.drawCenteredString(
                font,
                Component.literal("滑动").withStyle(ChatFormatting.GRAY)
                        .append(LoreHelper.wrap(Component.literal("鼠标滚轮").withStyle(ChatFormatting.GOLD)))
                        .append(Component.literal("以切换队友视角").withStyle(ChatFormatting.GRAY)),
                screenWidth / 2, screenHeight - font.lineHeight * 2,
                0xFFFFFFFF);
        poseStack.popPose();
        //渲染当前玩家信息
        renderSelectedPlayerInfo(gui, graphics, poseStack, cameraP, font, screenWidth, screenHeight);

        //渲染每条玩家信息

        int renderOffsetI = ClientGameData.aliveSameTeamPlayers.size();
        int teamColor = 0xFF000000 | player.getTeamColor();
        for (AbstractClientPlayer single : ClientGameData.aliveSameTeamPlayers) {
            poseStack.pushPose();
            poseStack.translate(cameraP == single ? 5F : 0F, renderOffsetI * -(getPlayerInfoHeight(screenWidth) + 2) + screenHeight - 4, 0);
            renderSinglePlayerInfo(graphics, poseStack, font, single, cameraP == single, teamColor, screenWidth, partialTick);
            renderOffsetI--;
            poseStack.popPose();
        }
    }
    public void renderSinglePlayerInfo(MegaGuiGraphics graphics, PoseStack poseStack, Font font, AbstractClientPlayer player, boolean isCamera, int teamColor, int screenWidth, float partialTicks) {
        float width = getPlayerInfoWidth(screenWidth, player);
        float height = getPlayerInfoHeight(screenWidth);
        MutableBoolean isAlive = new MutableBoolean(true);
        //渲染大背景
        graphics.fill(0, 0, width, height, 0xC8202020);
        //渲染左侧边框
        graphics.fill(0, 0, 3, height, teamColor);
        //渲染头像
        poseStack.pushPose();
        float profileSize = (height * 0.5F) * (1F / HEAD_LAYER_SCALE);
        poseStack.translate(3F + height * 0.5F, (height * 0.5F) , 0F);
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            isAlive.setValue(!cap.isXaeroDead());
            renderProfileIcon(player, graphics, profileSize, cap.isXaeroDead());
        });
        poseStack.popPose();
        if (isAlive.getValue())  {
            //渲染血条
            graphics.fill(3 + height, 0, width * (player.getHealth() / player.getMaxHealth()), height * 0.5F, teamColor);
            //渲染文字
            renderTextOnHealthBar(graphics, poseStack, player, font, width, height, Math.round(player.getHealth()));
            //渲染物品
            renderItems(graphics, poseStack, player, width, height);
            //渲染护甲
            renderArmor(graphics, player, height);
        } else {
            //渲染名字
            renderSingleDeathPlayerName(graphics, poseStack, player, font, width, height);
            //渲染KAD
            renderSingleDeathPlayerKAD(graphics, poseStack, player, font, width, height);
        }
        //渲染选中效果
        if (isCamera) {
            VertexConsumer vertexConsumer = graphics.bufferSource().getBuffer(RenderType.gui());
            fillGradientL2R(poseStack, vertexConsumer, width * 0.85F, 0, width * 1.25F, height, -1, 0xC89db3b5, 0x009db3b5);
            ((AccessorGuiGraphics) graphics).callFlushIfUnmanaged();
        }
    }
    public static void renderSelectedPlayerInfo(ForgeGui gui, MegaGuiGraphics graphics, PoseStack poseStack, AbstractClientPlayer player, Font font, float screenWidth, float screenHeight) {
        float rawSize = 22F;
        float profileSize = rawSize * (1F / HEAD_LAYER_SCALE);
        float width2 = 144F;
        float x = (screenWidth - width2) / 2F + profileSize;
        float y = Math.min(screenHeight * 0.82F, screenHeight - profileSize - 52F);
        String playerNameText = player.getDisplayName().getString();
        float width1 = -profileSize * HEAD_LAYER_SCALE + 144F - rawSize * HEAD_LAYER_SCALE;
        float height12 = 12F;
        poseStack.pushPose();
        poseStack.translate(x, y, 200F);
        renderProfileIcon(player, graphics, profileSize, false);
        poseStack.translate(0, 0, -10F);
        graphics.fill(-profileSize * HEAD_LAYER_SCALE, -profileSize * HEAD_LAYER_SCALE, profileSize * HEAD_LAYER_SCALE, profileSize * HEAD_LAYER_SCALE, 0xC8202020);
        int teamColor = 0xFF000000 | player.getTeamColor();
        //渲染名称条
        float barYOffset = (profileSize) / 2F;
        graphics.fill(rawSize * HEAD_LAYER_SCALE, barYOffset, rawSize * HEAD_LAYER_SCALE + width1, height12 + barYOffset, 0xC8202020);
        graphics.fill(rawSize * HEAD_LAYER_SCALE, barYOffset, rawSize * HEAD_LAYER_SCALE + 2, height12 + barYOffset, teamColor);
        graphics.fill(rawSize * HEAD_LAYER_SCALE + width1 - 2F, barYOffset, rawSize * HEAD_LAYER_SCALE + width1, height12 + barYOffset, teamColor);
        int nameY = Math.round(barYOffset + (-font.lineHeight + height12) / 2F);
        int nameX = Math.round(rawSize * HEAD_LAYER_SCALE + width1 / 2F);
        {
            float textWidthRatio = font.width(playerNameText) / (width1 - rawSize * HEAD_LAYER_SCALE);
            if (textWidthRatio <= 1.0F) textWidthRatio = 1.0F;
            poseStack.pushPose();
            poseStack.translate(0.5F + nameX - (font.width(playerNameText) / 2F / textWidthRatio), nameY, -0F);
            poseStack.scale(1 / textWidthRatio, 1 / textWidthRatio, 1F);
            graphics.drawString(font, playerNameText, 0, 0, 0xA0505050);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(nameX - (font.width(playerNameText) / 2F / textWidthRatio), nameY, -0F);
            poseStack.scale(1 / textWidthRatio, 1 / textWidthRatio, 1F);
            graphics.drawString(font, playerNameText, 0, 0, 0xFFFFFFFF);
            poseStack.popPose();
        }
        //渲染其他信息
        barYOffset += height12 + 1F;
        graphics.fill(-profileSize * HEAD_LAYER_SCALE, barYOffset, -profileSize * HEAD_LAYER_SCALE + width2, height12 + barYOffset, 0xC8202020);
        graphics.fill(-profileSize * HEAD_LAYER_SCALE, barYOffset, -profileSize * HEAD_LAYER_SCALE + 2, height12 + barYOffset, teamColor);
        graphics.fill(-profileSize * HEAD_LAYER_SCALE + width2 - 2F, barYOffset, -profileSize * HEAD_LAYER_SCALE + width2, height12 + barYOffset, teamColor);

        //渲染血量
        float iconSize = height12 * 0.8F;
        float iconXOffset = -profileSize * HEAD_LAYER_SCALE + 5;
        float iconY = barYOffset + (height12 - iconSize) / 2F;
        float iconTextYOffset = barYOffset + (height12 - font.lineHeight * 0.8F) / 2F;
        ModShaders.alphaFilterGray(0.5F);
        graphics.blit(HEART.texture(),
                iconXOffset, iconY,
                iconSize, iconSize,
                HEART.startX(), HEART.startY(),
                HEART.width(), HEART.height(),
                FPS_UI_RX, FPS_UI_RY,
                ModShaders::getAlphaFilter);
        graphics.blit(HEART.texture(),
                iconXOffset, iconY,
                iconSize, iconSize,
                HEART.startX(), HEART.startY(),
                HEART.width(), HEART.height(),
                FPS_UI_RX, FPS_UI_RY);
        iconXOffset += iconSize + 1;
        poseStack.pushPose();
        poseStack.translate(iconXOffset, iconTextYOffset, 0);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        String healthString = String.valueOf(Math.round(player.getHealth()));
        graphics.drawString(font, healthString, 0, 0, 0xFFFFFFFF, false);
        poseStack.popPose();
        iconXOffset += font.width(healthString) * 0.8F + 5;
        //渲染护甲
        ModShaders.alphaFilterGray(0.5F);
        graphics.blit(ARMOR.texture(),
                iconXOffset, iconY,
                iconSize, iconSize,
                ARMOR.startX(), ARMOR.startY(),
                ARMOR.width(), ARMOR.height(),
                FPS_UI_RX, FPS_UI_RY,
                ModShaders::getAlphaFilter);
        graphics.blit(ARMOR.texture(),
                iconXOffset, iconY,
                iconSize, iconSize,
                ARMOR.startX(), ARMOR.startY(),
                ARMOR.width(), ARMOR.height(),
                FPS_UI_RX, FPS_UI_RY);
        iconXOffset += iconSize + 1;
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        float armor = chestplate.isEmpty() ? 0F : (chestplate.getMaxDamage() - chestplate.getDamageValue()) / (float) chestplate.getMaxDamage();
        String armorString = String.valueOf((int) (armor * 100));
        poseStack.pushPose();
        poseStack.translate(iconXOffset, iconTextYOffset, 0);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        graphics.drawString(font, armorString, 0, 0, 0xFFFFFFFF, false);
        poseStack.popPose();
        //渲染KAD
        KAD currentKAD = ClientFpsData.getPlayerKAD(player).getOrDefaultKAD(KAD.KAD_CURRENT);
        Component kadContext =  Component.literal("K")
                .withStyle(style -> style.withColor(teamColor))
                .append(Component.literal(String.valueOf(currentKAD.kills)).withStyle(ChatFormatting.WHITE))
                .append("   A")
                .append(Component.literal(String.valueOf(currentKAD.assists)).withStyle(ChatFormatting.WHITE))
                .append("   D")
                .append(Component.literal(String.valueOf(currentKAD.deaths)).withStyle(ChatFormatting.WHITE));

        float realWidthOfKAD = font.width(kadContext) * 0.8F;
        iconXOffset = ((-profileSize * HEAD_LAYER_SCALE + width2) + iconXOffset + realWidthOfKAD) / 2F;
        iconSize = font.lineHeight;

        graphics.drawString(font, kadContext, iconXOffset - realWidthOfKAD - (float) (Math.random() - 0.65F) * (Math.random() < 0.2F ? 1.0F : 0F), iconY + (height12 - iconSize) / 2F, 0x80505050, false);
        graphics.drawString(font, kadContext, iconXOffset - realWidthOfKAD, iconY + (height12 - iconSize) / 2F, 0xFFFFFFFF, false);
        poseStack.popPose();
    }
    public static void renderArmor(MegaGuiGraphics graphics, AbstractClientPlayer player, float height) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        float completeness = chestplate.isEmpty() ? 0F : (chestplate.getMaxDamage() - chestplate.getDamageValue()) / (float) chestplate.getMaxDamage();
        float x = 1F + 4F + height;
        float y = height / 2F + 1F;
        float renderWidth = height / 2F - 2F;
        float renderHeight = height / 2F - 2F;
        RenderSystem.setShaderColor(1F, 1F, 1F, 0.5F);
        if (completeness <= 0F) {
            ModShaders.alphaFilterGray(0.5F);
            graphics.blit(ARMOR.texture(),
                    x, y,
                    renderWidth, renderHeight,
                    ARMOR.startX(), ARMOR.startY(),
                    ARMOR.width(), ARMOR.height(),
                    FPS_UI_RX, FPS_UI_RY,
                    ModShaders::getAlphaFilter);
        } else if (completeness >= 1F) {
            graphics.blit(ARMOR.texture(),
                    x, y,
                    renderWidth, renderHeight,
                    ARMOR.startX(), ARMOR.startY(),
                    ARMOR.width(), ARMOR.height(),
                    FPS_UI_RX, FPS_UI_RY);
        } else {
            ModShaders.alphaFilterGray(0.5F);
            graphics.blit(ARMOR.texture(),
                    x, y,
                    renderWidth, renderHeight,
                    ARMOR.startX(), ARMOR.startY(),
                    ARMOR.width(), ARMOR.height(),
                    FPS_UI_RX, FPS_UI_RY,
                    ModShaders::getAlphaFilter);
            renderCompletenessArmor(graphics,
                    x, y,
                    renderWidth, renderHeight,
                    completeness);
        }
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }
    public static void renderCompletenessArmor(MegaGuiGraphics graphics, float x, float y, float renderWidth, float renderHeight, float completeness) {
        float u1 = (float) ARMOR.endX() / FPS_UI_RX;
        float v1 = (float) ARMOR.endY() / FPS_UI_RY;
        float u0 = (float) ARMOR.startX() / FPS_UI_RX;
        float v0 = (float) ARMOR.startY() / FPS_UI_RY + ((float) ARMOR.height() / FPS_UI_RY * (1F - completeness));
        RenderSystem.setShaderTexture(0, ARMOR.texture());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, x, y + (renderHeight - renderHeight * completeness), 0).uv(u0, v0).endVertex();
        bufferbuilder.vertex(matrix4f, x, y + renderHeight, 0).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix4f, x + renderWidth, y + renderHeight, 0).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix4f, x + renderWidth, y + (renderHeight - renderHeight * completeness), 0).uv(u1, v0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
    public static void renderItems(MegaGuiGraphics graphics, PoseStack poseStack, AbstractClientPlayer player, float width, float height){
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            float screenRatio = (float) Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight();
            float sizeOfItem = 13F;
            float testRatio = (height / 2F / 12.5F);
            float xOffset = (width - sizeOfItem * testRatio - 2F);
            float itemRenderScale = testRatio * sizeOfItem / 16F;
            float yOffset = (-1 * (screenRatio * 4F)) + 8F / sizeOfItem;
            ItemStack itemStack = cap.getSlot0();
            //主武器
            if (!itemStack.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(xOffset, yOffset, 0);
                poseStack.scale(itemRenderScale, itemRenderScale, 1F);
                renderItemOrGun(graphics, player, itemStack, 0, 0);
                poseStack.popPose();
            }
            //副武器
            itemStack = cap.getSlot1();
            if (!itemStack.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(xOffset, yOffset + height / 2F + 1F, 0);
                poseStack.scale(itemRenderScale, itemRenderScale, 0F);
                renderItemOrGun(graphics, player, itemStack, 0, 0);
                poseStack.popPose();
            }
            //道具1234
            float lastItemRenderSize = sizeOfItem;
            sizeOfItem = 10F;
            xOffset = (width - sizeOfItem * testRatio - 2F);
            itemRenderScale = testRatio * sizeOfItem / 16F;
            yOffset = (-1 * (screenRatio * 4F)) + 8F / sizeOfItem ;
            int startIndex = 2;
            int endIndex = 5;
            for (int i=startIndex;i<endIndex+1;i++) {
                itemStack = cap.getSlot(i);
                if (!itemStack.isEmpty()) {
                    poseStack.pushPose();
                    poseStack.translate(xOffset - (i-startIndex+1) * 19.5F * itemRenderScale - 1 - lastItemRenderSize, yOffset + height / 2F + 2, 0);
                    poseStack.scale(itemRenderScale, itemRenderScale, 1F);
                    renderItemOrGun(graphics, player, itemStack, 0, 0);
                    poseStack.popPose();
                }
            }
        });
    }
    public static void renderTextOnHealthBar(MegaGuiGraphics graphics, PoseStack poseStack, AbstractClientPlayer player, Font font, float width, float height, int health) {
        //int maxWidthOfHealthText = Math.round(font.width("100") * textScale);
        int maxWidthText = (int) (width - 4 - height - 24);
        poseStack.pushPose();
        poseStack.scale(textScale, textScale, 1F);
        float yOffset = (height * 0.125F + 0.45F) / textScale;
        /*

        //渲染血条文本
        poseStack.pushPose();
        poseStack.translate((4 + height + 0.45F) / textScale, yOffset, 0);
        graphics.drawString(font, Component.literal(String.valueOf(health)), 0, 0, 0xBF505050, false);
        poseStack.translate(-0.45F / textScale, -0.45F / textScale, 0);
        graphics.drawString(font, Component.literal(String.valueOf(health)), 0, 0, 0xFFFFFFFF, false);
        poseStack.popPose();
         */
        //渲染名字
        String nameToString = player.getDisplayName().getString();
        float textWidthRatio = font.width(nameToString) / (float) maxWidthText;
        if (textWidthRatio <= 1.0F) textWidthRatio = 1.0F;
        poseStack.pushPose();
        poseStack.translate((6F + height + 0.45F) / textScale, yOffset, 0F);
        poseStack.scale(1F / textWidthRatio, 1F / textWidthRatio, 1F / textWidthRatio);
        graphics.drawString(font, Component.literal(nameToString), 0, 0, 0xBF505050, false);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate((6F + height) / textScale, yOffset-0.45F / textScale, 0F);
        poseStack.scale(1F / textWidthRatio, 1F / textWidthRatio, 1F / textWidthRatio);
        graphics.drawString(font, Component.literal(nameToString), 0, 0, 0xFFFFFFFF, false);
        poseStack.popPose();

        poseStack.popPose();

    }
    public static void renderSingleDeathPlayerName(MegaGuiGraphics graphics, PoseStack poseStack, AbstractClientPlayer player, Font font, float width, float height) {
        int maxWidthText = (int) (width - 4 - height - 24);
        poseStack.pushPose();
        poseStack.scale(textScale, textScale, 1F);
        float yOffset = (height * 0.125F + 0.45F) / textScale;
        //渲染名字
        String nameToString = player.getDisplayName().getString();
        float textWidthRatio = font.width(nameToString) / (float) maxWidthText;
        if (textWidthRatio <= 1.0F) textWidthRatio = 1.0F;

        poseStack.pushPose();
        poseStack.translate((6F + height) / textScale, yOffset-0.45F / textScale, 0F);
        poseStack.scale(1F / textWidthRatio, 1F / textWidthRatio, 1F / textWidthRatio);
        graphics.drawString(font, Component.literal(nameToString).withStyle(ChatFormatting.GRAY), 0, 0, 0xFFFFFFFF, false);
        poseStack.popPose();

        poseStack.popPose();

    }
    public static void renderSingleDeathPlayerKAD(MegaGuiGraphics graphics, PoseStack poseStack, AbstractClientPlayer player, Font font, float width, float height) {
        //渲染KAD
        float scale = 0.8F;
        float yStart = height * 0.15F;
        float yPeriod = height - yStart*2F - font.lineHeight*scale;
        float singleUnitWidth = 8 * scale;
        KAD currentKAD = ClientFpsData.getPlayerKAD(player).getOrDefaultKAD(KAD.KAD_CURRENT);
        float offsetK = width - (singleUnitWidth * 2) * 3;
        float offsetA = offsetK + singleUnitWidth * 2;
        float offsetD = offsetA + singleUnitWidth * 2;
        String dataS = "";

        poseStack.pushPose();
        dataS = "K";
        poseStack.translate(offsetK - font.width(dataS)*scale/2F, yStart, 0);
        poseStack.scale(scale, scale, 1F);
        graphics.drawString(font, dataS, 0, 0, 0xFFCCCCCC, false);
        poseStack.popPose();

        poseStack.pushPose();
        dataS = String.valueOf(currentKAD.kills);
        poseStack.translate(offsetK - font.width(dataS)*scale/2F, yPeriod+yStart, 0);
        poseStack.scale(scale, scale, 1F);
        graphics.drawString(font, dataS, 0F, 0F, 0xFFCCCCCC, false);
        poseStack.popPose();

        poseStack.pushPose();
        dataS = "A";
        poseStack.translate(offsetA - font.width(dataS)*scale/2F, yStart, 0);
        poseStack.scale(scale, scale, 1F);
        graphics.drawString(font, dataS, 0, 0, 0xFFCCCCCC, false);
        poseStack.popPose();

        poseStack.pushPose();
        dataS = String.valueOf(currentKAD.assists);
        poseStack.translate(offsetA - font.width(dataS)*scale/2F, yPeriod+yStart, 0);
        poseStack.scale(scale, scale, 1F);
        graphics.drawString(font, dataS, 0F, 0F, 0xFFCCCCCC, false);
        poseStack.popPose();

        poseStack.pushPose();
        dataS = "D";
        poseStack.translate(offsetD - font.width(dataS)*scale/2F, yStart, 0);
        poseStack.scale(scale, scale, 1F);
        graphics.drawString(font, dataS, 0, 0, 0xFFCCCCCC, false);
        poseStack.popPose();

        poseStack.pushPose();
        dataS = String.valueOf(currentKAD.deaths);
        poseStack.translate(offsetD - font.width(dataS)*scale/2F, yPeriod+yStart, 0);
        poseStack.scale(scale, scale, 1F);
        graphics.drawString(font, dataS, 0F, 0F, 0xFFCCCCCC, false);
        poseStack.popPose();
    }
    public static void renderProfileIcon(AbstractClientPlayer player, MegaGuiGraphics graphics,  float size, boolean gray) {
        size /= HEAD_LAYER_SCALE;
        if (Minecraft.getInstance().getConnection() == null) return;

        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID());
        ResourceLocation skin = playerInfo == null ? DefaultPlayerSkin.getDefaultSkin(player.getUUID()) : playerInfo.getSkinLocation();
        if (gray) {
            graphics.blit(skin, -size, -size, size * 2F, size * 2F, 8F, 8F, 8F, 8F, 64F, 64F, ModShaders::getGray);
            graphics.blit(skin, -size*(HEAD_LAYER_SCALE * 0.5F + 0.5F), -size*(HEAD_LAYER_SCALE * 0.5F + 0.5F), size * 2F * HEAD_LAYER_SCALE, size * 2F * HEAD_LAYER_SCALE, 40F, 8F, 8F, 8F, 64F, 64F, ModShaders::getGray);
        } else {
            graphics.blit(skin, -size, -size, size * 2F, size * 2F, 8F, 8F, 8F, 8F, 64F, 64F);
            graphics.blit(skin, -size*(HEAD_LAYER_SCALE * 0.5F + 0.5F), -size*(HEAD_LAYER_SCALE * 0.5F + 0.5F), size * 2F * HEAD_LAYER_SCALE, size * 2F * HEAD_LAYER_SCALE, 40F, 8F, 8F, 8F, 64F, 64F);
        }
    }
    public static void renderProfileIcon(ResourceLocation texture, MegaGuiGraphics graphics, float x, float y, float size, boolean gray) {
        size /= HEAD_LAYER_SCALE;
        size /= 2F;
        if (Minecraft.getInstance().getConnection() == null) return;

        if (gray) {
            graphics.blit(texture, -size + x, -size + y, size * 2F, size * 2F, 8F, 8F, 8F, 8F, 64F, 64F, ModShaders::getGray);
            graphics.blit(texture, -size*(HEAD_LAYER_SCALE * 0.5F + 0.5F) + x, -size*(HEAD_LAYER_SCALE * 0.5F + 0.5F) + y, size * 2F * HEAD_LAYER_SCALE, size * 2F * HEAD_LAYER_SCALE, 40F, 8F, 8F, 8F, 64F, 64F, ModShaders::getGray);
        } else {
            graphics.blit(texture, -size + x, -size + y, size * 2F, size * 2F, 8F, 8F, 8F, 8F, 64F, 64F);
            graphics.blit(texture, -size*(HEAD_LAYER_SCALE * 0.5F + 0.5F) + x, -size*(HEAD_LAYER_SCALE * 0.5F + 0.5F) + y, size * 2F * HEAD_LAYER_SCALE, size * 2F * HEAD_LAYER_SCALE, 40F, 8F, 8F, 8F, 64F, 64F);
        }
    }
    private float getPlayerInfoHeight(int screenWidth) {
        return (int) (Math.max(screenWidth * 0.22F, 45) * 0.18F);
    }
    private float getPlayerInfoWidth(int screenWidth, Player player) {
        MutableInt width = new MutableInt((int) Math.max(screenWidth * 0.24F, 45));
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            if (cap.isXaeroDead())
                width.setValue(width.getValue() * (1F - 0.14F));
        });
        return width.getValue();
    }
    private void fillGradientL2R(PoseStack poseStack, VertexConsumer vertexConsumer, float x, float y, float endx, float endy, float depth, int color1, int color2) {
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.vertex(matrix4f, x, y, depth).color(color1).endVertex();
        vertexConsumer.vertex(matrix4f, x, endy, depth).color(color1).endVertex();
        vertexConsumer.vertex(matrix4f, endx, endy, depth).color(color2).endVertex();
        vertexConsumer.vertex(matrix4f, endx, y, depth).color(color2).endVertex();
    }
    public static void renderItemOrGun(MegaGuiGraphics graphics, AbstractClientPlayer player, ItemStack itemStack, int x, int y) {
        if (itemStack.getItem() instanceof IGun) {
            Optional<GunDisplayInstance> optionalGunDisplayInstance = TimelessAPI.getGunDisplay(itemStack);
            optionalGunDisplayInstance.ifPresent(display -> {
                float color = equalsGun(player.getMainHandItem(), itemStack) ? 1F : 0.5F;
                RenderSystem.setShaderColor(color, color, color, 1F);
                graphics.blit(display.getHUDTexture(), x - 24F, y, 0.0F, 0.0F, 39, 13, 39, 13);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            });
            if (optionalGunDisplayInstance.isEmpty()) {
                graphics.renderItem(itemStack, x, y);
            }
        } else graphics.renderItem(itemStack, x, y);
    }
    public static boolean equalsGun(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack == itemStack2) return true;
        if (itemStack.hasTag() && itemStack2.hasTag()) {
            return itemStack.getTag().getString("GunId").equals(itemStack2.getTag().getString("GunId"));
        }
        return false;
    }
}
