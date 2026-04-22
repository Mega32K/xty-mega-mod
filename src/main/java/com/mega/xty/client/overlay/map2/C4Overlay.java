package com.mega.xty.client.overlay.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.client.screen.BlitInfo;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.renderer.BlurRectRenderer;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.item.component.C4BombItem;
import com.mega.xty.proxy.ClientProxy;
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

            } else {
                //若正在下包
                float progress = C4BombItem.getSettingProgress(mc.player, partialTick);
                if (progress >= 0.0F) {
                    renderBombSettingAnimation(mc.player, gui, MegaGuiGraphics.of(guiGraphics), progress, screenWidth, screenHeight);
                }
            }
        }
    }
    public void renderBombSettingAnimation(LocalPlayer player, ForgeGui gui, MegaGuiGraphics graphics, float progress, int screenWidth, int screenHeight) {
        float alpha = 1F - Easing.IN_OUT_CUBIC.calculate((1F - Math.min(1F, progress * 7F)));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        Font font = gui.getFont();
        float barWidth = screenWidth * 0.3F;
        float barHeight = barWidth * 0.1F;
        float x = (screenWidth - barWidth) / 2;
        float y = screenHeight * 0.75F;
        y += (-10 * (1F - alpha));
        int teamColor = FastColor.ARGB32.multiply(0xD8000000 | player.getTeamColor(), (int) (alpha * 255) << 24 | 0x00A00000 | 0x0000A000 | 0x000000A0);
        graphics.flush();
        //模糊条
        BlurRectRenderer.render(graphics, x, y, barWidth, barHeight, ((int)(alpha * 80 + 1) << 24 | 0x00300000 | 0x00003000 | 0x00000030), alpha * 8.0F);
        graphics.fill(x - 2, y, x, y + barHeight, teamColor);
        graphics.fill(x + barWidth, y, x + barWidth + 2, y + barHeight, teamColor);
        poseStack.popPose();
    }
}
