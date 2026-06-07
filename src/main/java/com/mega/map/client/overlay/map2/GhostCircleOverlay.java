package com.mega.map.client.overlay.map2;

import com.mega.endinglib.api.client.screen.BlitInfo;
import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.endinglib.util.time.TimeContext;
import com.mega.map.common.data.map2.ClientGame2Data;
import com.mega.map.common.init.ItemInit;
import com.mega.map.proxy.ClientProxy;
import com.mega.map.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.*;

import java.lang.Math;
import java.util.List;

public class GhostCircleOverlay implements IGuiOverlay {
    public static final BlitInfo OUTLINE = new BlitInfo(
            ClientProxy.ICONS,
            0, 76, 186, 9
    );
    public static final BlitInfo BAR = new BlitInfo(
            ClientProxy.ICONS,
            0, 85, 182, 5
    );
    public static final BlitInfo SOUL_RING = new BlitInfo(
            ClientProxy.ICONS,
            0, 90, 27, 32
    );
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (gui.getMinecraft().options.hideGui) return;
        if (ClientGame2Data.isStopped) return;
        gui.setupOverlayRenderState(true, false);
        if (Minecraft.getInstance().cameraEntity instanceof AbstractClientPlayer clientPlayer) {
            if (!clientPlayer.isSpectator()) {
                partialTick = TimeContext.safeClientFrameTime();
                float finalPartialTick = partialTick;
                CommonProxy.getMap2Cap(clientPlayer).ifPresent(cap -> {
                    if (!cap.isXaeroDead()) {
                        MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
                        //幽灵
                        if (clientPlayer.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get())) {
                            //渲染隐身状态条
                            renderInvisibleBar(gui, graphics, finalPartialTick, screenWidth, screenHeight, clientPlayer);
                        } else {
                            //渲染周遭幽灵环
                            renderGhostCircle(gui, graphics, finalPartialTick, screenWidth, screenHeight, clientPlayer);
                        }
                        RenderSystem.disableBlend();
                    }
                });
            }
        }
    }
    public void renderInvisibleBar(ForgeGui gui, MegaGuiGraphics graphics, float partialTick, int screenWidth, int screenHeight, AbstractClientPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel clientLevel = mc.level;
        if (player != null && clientLevel != null) {
            CommonProxy.getMap2Cap(player).ifPresent(capability -> {
                int left = screenWidth / 2 - OUTLINE.width() / 2;
                int top = screenHeight - gui.leftHeight + 6;
                {
                    Font font = gui.getFont();
                    String per = 100 - Math.round((capability.getInvisibleValue(partialTick) / 0.5F * 100)) + "%";
                    int textWidth = font.width(per);
                    graphics.drawString(font, per, left - textWidth, top - OUTLINE.height(), 0xA0cccccc);
                }
                graphics.blit(ClientProxy.ICONS, left, top - OUTLINE.height(), OUTLINE.startX(), OUTLINE.startY(), OUTLINE.width(), OUTLINE.height());

                float progress = capability.getInvisibleValue(partialTick) / 0.5F;
                progress = Mth.clamp(progress, 0F, 1F);
                float renderWidth = BAR.width() * progress;
                graphics.blit(ClientProxy.ICONS,
                        left + (OUTLINE.width() - BAR.width()) / 2F + BAR.width() / 2F - renderWidth / 2F, top - OUTLINE.height() + (OUTLINE.height() - BAR.height()) / 2F,
                        renderWidth, BAR.height(),
                        BAR.startX(), BAR.startY(),
                        BAR.width() * progress, BAR.height(),
                        256F, 256F);
                gui.leftHeight += 9;
            });
        }
    }
    public void renderGhostCircle(ForgeGui gui, MegaGuiGraphics graphics, float partialTick, int screenWidth, int screenHeight, AbstractClientPlayer clientPlayer) {
        List<Entity> entitiesAround;
        List<Point> renderPoints = new ObjectArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        ClientLevel clientLevel = mc.level;
        if (clientLevel != null) {
            entitiesAround = clientLevel.getEntities(
                    clientPlayer,
                    new AABB(clientPlayer.position(), clientPlayer.position())
                            .inflate(8),
                    (entity -> entity.distanceToSqr(clientPlayer) < 19.5F && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && !entity.isAlliedTo(clientPlayer))
            );
            Matrix4f matrix4f = new Matrix4f(ClientUtils.LEVEL_MODEL_VIEW_MAT);
            for (Entity entity : entitiesAround) {
                if (entity instanceof Player player) {
                    Vector4f v = matrix4f.transform(new Vector4f(
                            (float) Mth.lerp(partialTick, player.xOld - clientPlayer.xOld, player.getX() - clientPlayer.getX()),
                            (float) Mth.lerp(partialTick, player.yOld - clientPlayer.yOld, player.getY() - clientPlayer.getY()),
                            (float) Mth.lerp(partialTick, player.zOld - clientPlayer.zOld, player.getZ() - clientPlayer.getZ()),
                            0.0F
                    ));
                    Vector2d raw = new Vector2d(v.x, v.y).normalize();
                    MutableFloat distance = new MutableFloat(player.distanceTo(clientPlayer));
                    CommonProxy.getMap2Cap(player).ifPresent(cap -> {
                        distance.setValue(distance.getValue() * (1F - Mth.clamp(cap.getInvisibleValue(partialTick) / 0.5F, 0.0F, 0.5F)) * 2F);
                    });
                    renderPoints.add(new Point(raw.x, raw.y, distance.getValue()));
                }
            }
            PoseStack poseStack = graphics.pose();
            poseStack.translate(screenWidth / 2F, screenHeight / 2F, 0F);
            poseStack.pushPose();
            for (Point point : renderPoints) {
                float x = (float) point.x;
                float y = (float) point.z;
                double rot = Math.atan2(y, x);
                poseStack.pushPose();
                float xOffset = Mth.cos((float) rot) * 7f;
                float yOffset = Mth.sin((float) rot) * 7f;
                poseStack.translate(xOffset, -yOffset, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90F - (float) rot * Mth.RAD_TO_DEG ));
                float alpha = 0.5F / point.distance;
                graphics.setColor(1F, 1F, 1F, alpha);
                graphics.blit(SOUL_RING.texture(),
                        -SOUL_RING.width() / 2F, -SOUL_RING.height() / 2F,
                        SOUL_RING.width(), SOUL_RING.height(),
                        SOUL_RING.startX(), SOUL_RING.startY(),
                        SOUL_RING.width(), SOUL_RING.height(),
                        256, 256);
                poseStack.popPose();
            }
            graphics.setColor(1F, 1F, 1F, 1F);
            poseStack.popPose();
        }
    }
    record Point(double x, double z, float distance) {}
}
