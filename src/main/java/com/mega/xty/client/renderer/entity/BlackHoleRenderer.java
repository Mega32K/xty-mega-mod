package com.mega.xty.client.renderer.entity;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.entity.BlackHoleEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/entity/blackhole.png");

    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/entity/beam.png");
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BlackHoleEntity blackHoleEntity) {
        return TEXTURE;
    }

    public void render(BlackHoleEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight) {
        float entityScale = entity.getRadius(partialTicks);
        poseStack.pushPose();
        poseStack.translate(0.0, entity.getBoundingBox().getYsize() / 2.0, 0.0);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        poseStack.scale(0.5F * entityScale, 0.5F * entityScale, 0.5F * entityScale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        consumer.vertex(poseMatrix, 0.0F, -2.0F, -2.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, 0.0F, 2.0F, -2.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, 0.0F, 2.0F, 2.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, 0.0F, -2.0F, 2.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0, entity.getBoundingBox().getYsize() / 2.0, -0.2F);

        float animationProgress = ((float)entity.tickCount + partialTicks) / 700.0F;
        float fadeProgress = 0.5F;
        RandomSource randomSource = RandomSource.create(432L);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.energySwirl(BEAM_TEXTURE, 0.0F, 0.0F));

        float segments = Math.min(animationProgress * 0.3F, 0.8F);

        poseStack.mulPose(Axis.ZP.rotationDegrees(90));
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        for(int i = 0; (float)i < (segments + segments * segments) / 2.0F * 60.0F; ++i) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(randomSource.nextFloat() * 360.0F + animationProgress * 90.0F));
            float size1 = (randomSource.nextFloat() * 10.0F + 5.0F + fadeProgress * 5.0F) * entityScale * 0.4F;
            Matrix4f matrix = poseStack.last().pose();
            Matrix3f normalMatrix2 = poseStack.last().normal();
            drawTriangle(vertexConsumer, matrix, normalMatrix2, size1);
        }
        poseStack.popPose();
    }

    private static void drawTriangle(VertexConsumer consumer, Matrix4f poseMatrix, Matrix3f normalMatrix, float size) {
        consumer.vertex(poseMatrix, 0.0F, 0.0F, 0.0F).color(255, 0, 255, 45).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, -0.065F * size, 1F * size, 0.0F).color(0, 0, 0, 0).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, 0.065F * size, 1F * size, 0.0F).color(0, 0, 0, 0).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, 0.0F, 0.0F, 0.0F).color(255, 0, 255, 45).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
