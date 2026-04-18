package com.mega.xty.mixin.map2;

import com.mega.xty.util.data_expand.PlayerRendererCaller;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> implements PlayerRendererCaller {
    @Shadow protected abstract void setModelProperties(AbstractClientPlayer p_117819_);

    public PlayerRendererMixin(EntityRendererProvider.Context p_174289_, PlayerModel<AbstractClientPlayer> p_174290_, float p_174291_) {
        super(p_174289_, p_174290_, p_174291_);
    }

    @Override
    public void renderWithoutEvents(AbstractClientPlayer clientPlayer, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        this.setModelProperties(clientPlayer);
        super.render(clientPlayer, yRot, partialTicks, poseStack, bufferSource, light);
    }
}
