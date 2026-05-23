package com.mega.map.client.overlay.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.client.screen.BlitInfo;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.map.client.shader.ModShaders;
import com.mega.map.common.data.map2.ClientGameData;
import com.mega.map.proxy.ClientProxy;
import com.mega.map.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.lwjgl.opengl.GL11;

public class HealthOverlay implements IGuiOverlay {
    public static final BlitInfo BACKGROUND = new BlitInfo(
            ClientProxy.ICONS,
            0, 14, 101, 62
    );
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (!ClientGameData.shouldRenderCFHealth()) return;
        Minecraft mc = gui.getMinecraft();
        if (mc.player != null) {
            CommonProxy.getMap2Cap(mc.player).ifPresent(cap -> {
                if (!cap.isXaeroDead()) {
                    if (mc.getCameraEntity() instanceof AbstractClientPlayer clientPlayer) {
                        float animProgress = Easing.IN_OUT_CUBIC.calculate(Math.min((ClientGameData.CFHealthTickCount + partialTick) / 20F, 1F));
                        gui.setupOverlayRenderState(true, true);
                        MegaGuiGraphics graphics = new MegaGuiGraphics(mc, guiGraphics.bufferSource());
                        PoseStack poseStack = graphics.pose();
                        Font font = gui.getFont();
                        poseStack.pushPose();
                        poseStack.translate((animProgress - 1F) * 200F, 0, 1000F);
                        //渲染背景
                        renderBackground(graphics, poseStack);
                        //渲染头像
                        float headX = 19F;
                        float headY = graphics.guiHeight() - 40F;
                        poseStack.pushPose();
                        poseStack.translate(headX, headY, 37F);
                        renderProfileIconWithOutline(clientPlayer, graphics, poseStack);
                        poseStack.popPose();
                        //渲染名字
                        float nameScale = 0.8F;
                        poseStack.pushPose();
                        poseStack.translate(6, headY + 14.5F, 0F);
                        poseStack.scale(nameScale, nameScale, nameScale);
                        drawWordWrap(graphics,
                                font,
                                clientPlayer.getDisplayName(),
                                0,
                                0,
                                36,
                                0xFFFFFF
                        );
                        poseStack.popPose();
                        poseStack.pushPose();
                        //渲染护甲和血量
                        poseStack.translate(headX + 22, headY - 11, 0);
                        renderACHP(graphics, font, clientPlayer, poseStack);
                        poseStack.popPose();
                        poseStack.popPose();
                        RenderSystem.disableBlend();
                        RenderSystem.disableDepthTest();
                    }
                }
            });
        }
    }
    public static void renderBackground(MegaGuiGraphics graphics, PoseStack poseStack) {
        int guiHeight = graphics.guiHeight();
        poseStack.pushPose();
        graphics.blit(ClientProxy.ICONS, 1, guiHeight - BACKGROUND.height() - 2, BACKGROUND.startX(), BACKGROUND.startY(), BACKGROUND.width(), BACKGROUND.height());
        graphics.blit(ClientProxy.ICONS, 4, guiHeight - BACKGROUND.height() + 3 - 2, 0, 0, BACKGROUND.endX() - 6, BACKGROUND.endY() - 20, ModShaders::getMapHealthBackground);
        poseStack.popPose();
    }
    public static void renderProfileIconWithOutline(AbstractClientPlayer player, MegaGuiGraphics graphics, PoseStack poseStack) {
        graphics.setColor(0.2F, 0.2F, 0.2F, 0.5F);
        RenderSystem.depthFunc(GL11.GL_LESS);
        for (int i=-1;i<=1;i++) {
            for (int j=-1;j<=1;j++) {
                poseStack.pushPose();
                poseStack.translate(i, j, -1F);
                poseStack.scale(1.1F, 1.1F, 1.1F);
                renderProfileIcon(player, graphics, poseStack, 11F);
                poseStack.popPose();
            }
        }
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        graphics.setColor(1F, 1F, 1F, 1F);

        renderProfileIcon(player, graphics, poseStack, 11F);
    }
    public static void renderProfileIcon(AbstractClientPlayer player, MegaGuiGraphics graphics, PoseStack poseStack, float size) {
        poseStack.pushPose();
        ResourceLocation skin = player.getSkinTextureLocation();
        //渲染第一层
        graphics.blit(skin, -size, -11F, size * 2F, size * 2F, 8F, 8F, 8F, 8F, 64F, 64F);
        poseStack.scale(1.0625F, 1.0625F, 0F);
        graphics.blit(skin, -size, -11F, size * 2F, size * 2F, 40F, 8F, 8F, 8F, 64F, 64F);
        poseStack.popPose();
    }
    public static void renderACHP(MegaGuiGraphics graphics, Font font, AbstractClientPlayer clientPlayer, PoseStack poseStack) {
        int backgroundWidth = Math.max(font.width("AC   100"), font.width("HP   100"));
        poseStack.pushPose();
        //背景
        graphics.fillGradient(-1, -1,  backgroundWidth + 1, font.lineHeight + 1, 0x9C666666, 0x10666666);
        graphics.fill(-1, -1,  backgroundWidth + 1, font.lineHeight + 1, 0x663a5595);
        //文本
        poseStack.scale(0.75F, 0.75F, 0.75F);
        graphics.drawString(font, " AC", 0, 0, 0xDDFFFFFF);
        poseStack.popPose();
        ItemStack chestplate = clientPlayer.getItemBySlot(EquipmentSlot.CHEST);
        int displayAC = Mth.ceil((chestplate.getMaxDamage() - chestplate.getDamageValue()) / (float) chestplate.getMaxDamage() * 100);
        graphics.drawString(font, "    "+displayAC, 0, 0, 0xFFFFFFFF);

        poseStack.pushPose();
        poseStack.translate(0, 14, 0);
        //背景
        graphics.fillGradient(-1, -1,  backgroundWidth + 1, font.lineHeight + 1, 0x9C666666, 0x10666666);
        graphics.fill(-1, -1,  backgroundWidth + 1, font.lineHeight + 1, 0x663a5595);
        //文本
        poseStack.scale(0.75F, 0.75F, 0.75F);
        graphics.drawString(font, " HP", 0, 0, 0xDDFFFFFF);
        poseStack.popPose();
        poseStack.translate(0, 14, 0);
        int displayHealth = Mth.ceil(clientPlayer.getHealth());
        graphics.drawString(font, "    "+displayHealth, 0, 0, 0xFFFFFFFF);
    }
    public void drawWordWrap(GuiGraphics graphics, Font font, FormattedText text, int x, int y, int maxLength, int color) {
        for(FormattedCharSequence formattedcharsequence : font.split(text, maxLength)) {
            graphics.drawString(font, formattedcharsequence, x, y, color);
            y += 9;
        }
    }
}
