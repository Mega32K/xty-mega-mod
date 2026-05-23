package com.mega.map.client.renderer.entity;

import com.mega.endinglib.util.SafeClass;
import com.mega.map.common.entity.ShadowPlayerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ShadowPlayerRenderer extends LivingEntityRenderer<ShadowPlayerEntity, PlayerModel<ShadowPlayerEntity>> {
    public ShadowPlayerRenderer(EntityRendererProvider.Context p_174289_) {
        super(p_174289_, null, .5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowPlayerEntity p_114482_) {
        Player player = p_114482_.getBind();
        if (player instanceof AbstractClientPlayer cp) {
            return cp.getSkinTextureLocation();
        }
        return null;
    }

    @Override
    public boolean shouldRender(ShadowPlayerEntity p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        return true;
    }

    @Override
    public void render(@NotNull ShadowPlayerEntity shadowPlayerEntity, float p_115309_, float pTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (SafeClass.usingShaderPack()) {
            packedLight = (int) (0xFF00F0 * 1.3F);
        } else packedLight = 0xFF00F0;
        if (shadowPlayerEntity.getBind() instanceof AbstractClientPlayer clientPlayer && shadowPlayerEntity.renderer instanceof WrappedPlayerRenderer wrappedPlayerRenderer) {
            Minecraft minecraft = Minecraft.getInstance();
            EntityRenderer<? super AbstractClientPlayer> renderer = minecraft.getEntityRenderDispatcher().getRenderer(clientPlayer);
            poseStack.pushPose();
            Vec3 xyzOffset = new Vec3(
                    Mth.lerp(0.5F, shadowPlayerEntity.sXOld, shadowPlayerEntity.sX)-Mth.lerp(pTicks, shadowPlayerEntity.xOld, shadowPlayerEntity.getX()),
                    Mth.lerp(0.5F, shadowPlayerEntity.sYOld, shadowPlayerEntity.sY)-Mth.lerp(pTicks, shadowPlayerEntity.yOld, shadowPlayerEntity.getY()),
                    Mth.lerp(0.5F, shadowPlayerEntity.sZOld, shadowPlayerEntity.sZ)-Mth.lerp(pTicks, shadowPlayerEntity.zOld, shadowPlayerEntity.getZ())
            );
            poseStack.translate(xyzOffset.x, xyzOffset.y, xyzOffset.z);
            int tickCount = clientPlayer.tickCount;
            clientPlayer.tickCount = shadowPlayerEntity.frozenTick;
            wrappedPlayerRenderer.render(shadowPlayerEntity ,clientPlayer, p_115309_, 0.5F, poseStack, bufferSource, packedLight);
            clientPlayer.tickCount = tickCount;
            poseStack.popPose();
        }
    }
}
