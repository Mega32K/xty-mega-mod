package com.mega.map.client.overlay.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.client.screen.BlitInfo;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.map.MegaMod;
import com.mega.map.common.data.map2.ClientGame1Data;
import com.mega.map.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class Game1Overlay implements IGuiOverlay {
    public static final BlitInfo EMPTY_SKULL = new BlitInfo(
            ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "textures/ui/empty_skull.png"),
            0, 0, 512, 512
    );
    public static final BlitInfo SKULL = new BlitInfo(
            ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "textures/ui/skull.png"),
            0, 0, 512, 512
    );
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
    }
    public void render2(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight, float yOffset) {
        if (!ClientGame1Data.playing()) return;
        if (ClientWrapped.clientPlayer() instanceof AbstractClientPlayer cp) {
            CommonProxy.getMap2Cap(cp).ifPresent(capability -> {
                MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
                float animProgress = Easing.IN_OUT_CUBIC.calculate(Math.min((ClientGame1Data.tickCount + partialTick) / 20F, 1F));
                float yAnimationOffset = (animProgress - 1F) * 80F;
                Font font = gui.getFont();
                int midX = screenWidth / 2;
                ItemStack left = capability.clientEvolutionWeapons.get(0);
                ItemStack mid = capability.clientEvolutionWeapons.get(1);
                ItemStack right = capability.clientEvolutionWeapons.get(2);
                PoseStack poseStack = graphics.pose();
                poseStack.translate(0, yOffset + yAnimationOffset, 0);
                int size = 6;
                int xOffset = midX - 44;
                //渲染武器进化表
                graphics.setColor(1F, 1F, 1F, animProgress);
                drawArrow(graphics, font, xOffset + size, -size / 2);
                drawLvl(graphics, poseStack, font, xOffset, size, capability.getEvolutionIndex() + 1, size);
                renderItemCentered(graphics, poseStack, left, xOffset, 0, size, true);

                xOffset += 44;
                drawArrow(graphics, font, xOffset + size, -size / 2);
                drawLvl(graphics, poseStack, font, xOffset, size, capability.getEvolutionIndex() + 2, size);
                renderItemCentered(graphics, poseStack, mid, xOffset, 0, size, false);

                xOffset += 44;
                drawLvl(graphics, poseStack, font, xOffset, size, capability.getEvolutionIndex() + 3, size);
                renderItemCentered(graphics, poseStack, right, xOffset, 0, size, false);
                //渲染头颅
                int skullPeriod = 24;
                int skullRenderSize = 24;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                for (int i=-2;i<2;i++) {
                    int skullNum = i + 3;
                    graphics.blit(skullNum <= capability.getEvolutionKillCount() ? SKULL.texture() : EMPTY_SKULL.texture(),
                            midX + ((float) i ) * skullPeriod, 20,
                            skullRenderSize, skullRenderSize,
                            SKULL.startX(), SKULL.startY(),
                            SKULL.endX(), SKULL.endY(),
                            SKULL.width(), SKULL.height());
                }
                graphics.setColor(1F, 1F, 1F, 1F);
            });
        }
    }
    private void renderItemCentered(MegaGuiGraphics graphics, PoseStack poseStack, ItemStack itemStack, int x, int y, int size, boolean selected) {
        poseStack.pushPose();
        poseStack.translate(-4F, -4F, 0F);
        if (selected) {
            graphics.fill(x - size - 2F, y - size - 2F, x + size * 2 + 2F, y + size * 2 + 2F, 0xA0424851);
            graphics.fillGradient(x - size - 2, y - size - 2, x + size * 2 + 2, y + size * 2 + 2, 0xA0f38751, 0x00f38751);
            graphics.renderOutline(x - size - 2, y - size - 2, size * 3 + 4, size * 3 + 4, 0xA0ffca18);
        } else {
            graphics.fill(x - size - 2, y - size - 2, x + size * 2 + 2F, y + size * 2 + 2F, 0xA0424851);
            graphics.renderOutline(x - size - 2, y - size - 2, size * 3 + 4, size * 3 + 4, 0xA06c6c6c);
        }
        poseStack.translate(-4.5F, -4.5F, 0F);
        graphics.renderItem(itemStack, x, y);
        poseStack.popPose();
    }
    private void drawLvl(MegaGuiGraphics graphics, PoseStack poseStack, Font font, int x, int y, int lvl, int size) {
        String text = "Lv." + lvl;
        poseStack.pushPose();
        poseStack.translate(x - 1, y + 5, 0);
        poseStack.scale(0.8F, 0.8F, 1F);
        graphics.drawCenteredString(font, text, 0, 0, 0xFFFFFFFF);
        poseStack.popPose();
    }
    private void drawArrow(MegaGuiGraphics graphics, Font font, int x, int y) {
        graphics.drawString(font, "  >", x, y, 0x50f38751);
        graphics.drawString(font, "  >", x + 5, y, 0xA0f38751);
        graphics.drawString(font, "  >", x + 10, y, 0xF0f38751);
    }
}
