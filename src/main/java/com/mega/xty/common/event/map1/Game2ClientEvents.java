package com.mega.xty.common.event.map1;

import com.mega.endinglib.proxy.CommonProxy;
import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.xty.common.data.map1.ClientGame2Data;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.c2s.map1.game2.C2SPlayerLaydownPacket;
import com.mega.xty.common.network.c2s.map1.game2.C2SPlayerSwingHandNoticePacket;
import com.mega.xty.game2.ShakeHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class Game2ClientEvents {
    public static int sprintCooldown = 0;
    public static Vec2 transform(Matrix4f matrix4f, Vector3f pos) {
        Minecraft mc = Minecraft.getInstance();
        pos = pos.add(mc.gameRenderer.getMainCamera().getPosition().toVector3f().mul(-1).add(0.5F, 0.F, 0.5F));
        Vector4f v4 = matrix4f.transform(new Vector4f(pos, 0.0F));
        return new Vec2((v4.x/v4.z+1)/2f, (v4.y/v4.z+1)/2f);
    }
    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (ClientGame2Data.sceneChanging) return;
        if (ClientGame2Data.isStopped) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.screen != null) return;
        com.mega.xty.proxy.CommonProxy.getXtyCap(player).ifPresent(c3 -> {
            if (c3.getGame2Health() > 0F) {
                CommonProxy.getCameraCapOptional(player).ifPresent(capability -> {
                    if (capability.isMouseControlled()) {
                        com.mega.xty.proxy.CommonProxy.getXtyCap(player).ifPresent(c2 -> {
                            Matrix4f matrix = new Matrix4f(ClientUtils.LEVEL_PROJ_MAT).mul(ClientUtils.LEVEL_MODEL_VIEW_MAT);
                            if (event.getAction() == GLFW.GLFW_PRESS) {
                                if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && sprintCooldown <= 0) {
                                    ItemStack mainhand = player.getMainHandItem();
                                    if (!ModList.get().isLoaded("tacz") || !TaczCommonEvents.isItemStackGun(mainhand)) {
                                        double i = mc.mouseHandler.xpos() / (double) mc.getWindow().getWidth();
                                        double j = mc.mouseHandler.ypos() / (double) mc.getWindow().getHeight();
                                        float headPosX = (float) player.position().x;
                                        float headPosY = (float) player.position().y + player.getEyeHeight(player.getPose());
                                        float headPosZ = (float) player.position().z;
                                        Vec2 transformedHeadPos = transform(matrix, new Vector3f(headPosX, headPosY, headPosZ));
                                        Vec2 arrow = new Vec2((float) i - transformedHeadPos.x, (float) j - transformedHeadPos.y).normalized();
                                        arrow = new Vec2(arrow.x, j > transformedHeadPos.y ? Math.abs(arrow.y) : -Math.abs(arrow.y));
                                        if (c2.xlCollision)
                                            arrow = arrow.scale(3.75F);
                                        else if (player.getDeltaMovement().y > -0.5F && !player.verticalCollisionBelow) {
                                            arrow = new Vec2(arrow.x * 4.75F, arrow.y);
                                            sprintCooldown = 20;
                                        }
                                        if (c2.xlCollision)
                                            NetworkHandler.sendToServer(new C2SPlayerLaydownPacket());
                                        if (player.getDeltaMovement().horizontalDistance() < 0.25F) {
                                            ShakeHandler.shake(0.2F, arrow, 8, 2);
                                            player.setDeltaMovement(player.getDeltaMovement().add(arrow.x * 0.25F, arrow.y * 0.25F, 0));
                                        }

                                        sprintCooldown = 10;
                                    }
                                } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && sprintCooldown <= 1) {
                                    double i = mc.mouseHandler.xpos() / (double)mc.getWindow().getWidth();
                                    double j = mc.mouseHandler.ypos() / (double)mc.getWindow().getHeight();
                                    float headPosX = (float) player.position().x;
                                    float headPosY = (float)  player.position().y + player.getEyeHeight(player.getPose());
                                    float headPosZ = (float)  player.position().z;
                                    Vec2 transformedHeadPos = transform(matrix, new Vector3f(headPosX, headPosY, headPosZ));
                                    Vec2 arrow = new Vec2((float) i - transformedHeadPos.x, (float) j - transformedHeadPos.y).normalized();
                                    arrow = new Vec2(arrow.x, j > transformedHeadPos.y ? Math.abs(arrow.y) : -Math.abs(arrow.y));
                                    if (c2.xlCollision)
                                        arrow = arrow.scale(3.75F);
                                    else if (player.getDeltaMovement().y > -0.5F && !player.verticalCollisionBelow) {
                                        arrow = new Vec2(arrow.x * 4.75F, arrow.y);
                                        sprintCooldown = 20;
                                    }
                                    if (c2.xlCollision)
                                        NetworkHandler.sendToServer(new C2SPlayerLaydownPacket());
                                    if (player.getDeltaMovement().horizontalDistance() < 0.25F) {
                                        ShakeHandler.shake(0.15F, arrow, 8, 2);
                                        player.setDeltaMovement(player.getDeltaMovement().add(arrow.x * 0.2F,  arrow.y * 0.2F, 0));
                                    }

                                    NetworkHandler.sendToServer(new C2SPlayerSwingHandNoticePacket());
                                    sprintCooldown = 10;
                                }
                            }
                        });
                    }
                });
            }
        });

    }
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (ClientGame2Data.isStopped) return;
            if (ClientGame2Data.sceneChanging) return;
            if (sprintCooldown > 0)
                sprintCooldown--;
            ClientGame2Data.tick(Minecraft.getInstance().level);
        }
    }
    public static void renderRegularPolygon(PoseStack stack, float radius, float sides, float width, int packedLight, float r, float g, float b, float a, VertexConsumer vertexConsumer, float percentage, boolean isInLevel) {
        float PI = 3.1415926F;
        sides /= 2;
        Matrix4f m = stack.last().pose();
        Matrix3f matrix3f = stack.last().normal();
        Vector4f color = new Vector4f(r, g, b, a);
        float alpha;
        for (alpha = 0.0F; alpha < 2F * PI; alpha += PI / sides) {
            if (percentage < 1.0F && alpha / (2F * PI) >= percentage)
                break;
            double cos = Mth.cos(alpha);
            double sin = Mth.sin(alpha);
            double cos_ = Mth.cos(alpha + PI / sides);
            double sin_ = Mth.sin(alpha + PI / sides);
            float x = (float) (radius * cos);
            float y = (float) (radius * sin);
            vertexRP(vertexConsumer, m, matrix3f, packedLight, x, y, 0, 0, color);
            x = (float) (radius * cos_);
            y = (float) (radius * sin_);
            vertexRP(vertexConsumer, m, matrix3f, packedLight, x, y, 0, 0, color);
            x = (float) ((radius - width) * cos_);
            y = (float) ((radius - width) * sin_);
            vertexRP(vertexConsumer, m, matrix3f, packedLight, x, y, 0, 0, color);
            x = (float) ((radius - width) * cos);
            y = (float) ((radius - width) * sin);
            vertexRP(vertexConsumer, m, matrix3f, packedLight, x, y, 0, 0, color);
        }
    }
    public static void vertexRP(VertexConsumer p_254464_, Matrix4f p_254085_, Matrix3f p_253962_, int light, float x, float y, float u, float v, Vector4f color) {
        p_254464_.vertex(p_254085_, x, y, 0.0F).color(color.x, color.y, color.z, color.w).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(p_253962_, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
