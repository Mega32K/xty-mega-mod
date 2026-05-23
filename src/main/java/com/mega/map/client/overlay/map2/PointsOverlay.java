package com.mega.map.client.overlay.map2;

import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.map.common.data.map2.ClientGameData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class PointsOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (ClientGameData.isStopped) return;
        BlockPos pa = ClientGameData.pointA;
        BlockPos pb = ClientGameData.pointB;
        if (pa != null || pb != null) {
            gui.setupOverlayRenderState(true, false);
            Vec3 pPos = gui.getMinecraft().gameRenderer.getMainCamera().getPosition();
            Matrix4f matrix4f = new Matrix4f().identity().mul(ClientUtils.LEVEL_PROJ_MAT).mul(ClientUtils.LEVEL_MODEL_VIEW_MAT);
            Font font = gui.getFont();
            PoseStack poseStack = guiGraphics.pose();
            PA : {
                if (pa != null) {
                    Vec2 screenPos = projectToScreen(matrix4f, pa.getCenter(), screenWidth, screenHeight);
                    screenPos = new Vec2(screenPos.x, Math.min(screenPos.y, screenHeight - font.lineHeight * 2));
                    String text1 = Math.round((float) pPos.distanceTo(pa.getCenter())) + "m";
                    String text2 = ClientGameData.aText;
                    poseStack.pushPose();
                    poseStack.translate(screenPos.x, screenPos.y, 0);
                    guiGraphics.drawCenteredString(font, text1, 0, 0, 0xA0FFFFFF);
                    font.drawInBatch8xOutline(FormattedCharSequence.forward(text2, Style.EMPTY),
                            -font.width(text2) / 2F, font.lineHeight,
                            0xA0FFFFFF, 0xA06ea8c5,
                            poseStack.last().pose(), guiGraphics.bufferSource(), 255);
                    poseStack.popPose();
                }
            }
            PB : {
                if (pb != null) {
                    Vec2 screenPos = projectToScreen(matrix4f, pb.getCenter(), screenWidth, screenHeight);
                    screenPos = new Vec2(screenPos.x, Math.min(screenPos.y, screenHeight - font.lineHeight * 2));
                    String text1 = Math.round((float) pPos.distanceTo(pb.getCenter())) + "m";
                    String text2 = ClientGameData.bText;
                    poseStack.pushPose();
                    poseStack.translate(screenPos.x, screenPos.y, 0);
                    guiGraphics.drawCenteredString(font, text1, 0, 0, 0xA0FFFFFF);
                    font.drawInBatch8xOutline(FormattedCharSequence.forward(text2, Style.EMPTY),
                            -font.width(text2) / 2F, font.lineHeight,
                            0xA0FFFFFF, 0xA06ea8c5,
                            poseStack.last().pose(), guiGraphics.bufferSource(), 255);
                    poseStack.popPose();
                }
            }
            RenderSystem.disableBlend();
        }
    }

    /**
     * 将世界坐标中的点通过关卡渲染使用的投影矩阵与模型视图矩阵转换为屏幕坐标。
     *
     * @param matrix {@code projection * modelView} 的组合矩阵
     * @param worldPos 世界坐标中的位置点
     * @param screenWidth 当前 GUI 层使用的屏幕宽度
     * @param screenHeight 当前 GUI 层使用的屏幕高度
     * @return 转换后的屏幕坐标；若点位于相机后方或超出屏幕范围，则会按方向投影到屏幕边框上
     */
    private static Vec2 projectToScreen(Matrix4f matrix, Vec3 worldPos, int screenWidth, int screenHeight) {
        Vector4f clipPos = matrix.transform(new Vector4f(worldPos.toVector3f(), 1.0F));
        float w = Math.max(Math.abs(clipPos.w), 0.00001F);
        float ndcX = clipPos.x / w;
        float ndcY = clipPos.y / w;
        boolean forceBorder = clipPos.w <= 0.0F || Math.abs(ndcX) > 1.0F || Math.abs(ndcY) > 1.0F;
        if (forceBorder) {
            float scale = Math.max(Math.abs(ndcX), Math.abs(ndcY));
            if (scale <= 0.00001F) {
                ndcX = 0.0F;
                ndcY = 1.0F;
            } else {
                ndcX /= scale;
                ndcY /= scale;
            }
        }
        float screenX = Mth.clamp(screenWidth * ((ndcX + 1.0F) * 0.5F), 8.0F, screenWidth - 8.0F);
        float screenY = Mth.clamp(screenHeight * (1.0F - (ndcY + 1.0F) * 0.5F), 8.0F, screenHeight - 8.0F);
        return new Vec2(screenX, screenY);
    }
}
