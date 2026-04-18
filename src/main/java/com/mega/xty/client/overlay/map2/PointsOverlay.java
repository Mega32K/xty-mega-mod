package com.mega.xty.client.overlay.map2;

import com.github.franckyi.ibeeditor.client.ClientUtil;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix4f;
import org.joml.Vector2i;
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
                    Vector4f pos = matrix4f.transform(new Vector4f(pa.getCenter().add(pPos.scale(-1F)).toVector3f(), 0F));
                    pos = new Vector4f((pos.x/pos.z+1)/2f, (pos.y/pos.z+1)/2f, pos.z, 1);
                    if (pos.z < 0F)
                        break PA;
                    Vec2 screenPos = new Vec2(Mth.clamp(screenWidth * pos.x, 8, screenWidth - 8), Mth.clamp(screenHeight - screenHeight * pos.y, 8, screenHeight - 8));
                    String text1 = Math.round((float) pPos.distanceTo(pa.getCenter())) + "m";
                    String text2 = "A";
                    poseStack.pushPose();
                    poseStack.translate(screenPos.x, screenPos.y, 1000);
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
                    Vector4f pos = matrix4f.transform(new Vector4f(pb.getCenter().add(pPos.scale(-1F)).toVector3f(), 0F));
                    pos = new Vector4f((pos.x/pos.z+1)/2f, (pos.y/pos.z+1)/2f, pos.z, 1);
                    if (pos.z < 0F)
                        break PB;
                    Vec2 screenPos = new Vec2(Mth.clamp(screenWidth * pos.x, 8, screenWidth - 8), Mth.clamp(screenHeight - screenHeight * pos.y, 8, screenHeight - 8));
                    String text1 = Math.round((float) pPos.distanceTo(pb.getCenter())) + "m";
                    String text2 = "B";
                    poseStack.pushPose();
                    poseStack.translate(screenPos.x, screenPos.y, 1000);
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
}
