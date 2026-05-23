package com.mega.map.mixin.map2.game2;

import com.google.common.base.MoreObjects;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.map.proxy.CommonProxy;
import com.mega.map.util.data_expand.OpticalNanoInvisibleItemInHandRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin implements OpticalNanoInvisibleItemInHandRenderer {
    @Shadow
    static ItemInHandRenderer.HandRenderSelection evaluateWhichHandsToRender(LocalPlayer p_172915_) {
        throw new AssertionError("");
    }

    @Shadow protected abstract void renderArmWithItem(AbstractClientPlayer p_109372_, float p_109373_, float p_109374_, InteractionHand p_109375_, float p_109376_, ItemStack p_109377_, float p_109378_, PoseStack p_109379_, MultiBufferSource p_109380_, int p_109381_);

    @Shadow private float oOffHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;
    @Shadow private float oMainHandHeight;
    @Shadow private float mainHandHeight;
    @Override
    public void opticalNanoInvisible(float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int light) {
        Player localP = ClientWrapped.clientPlayer();
        MultiBufferSource.BufferSource mcBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        CommonProxy.getMap2Cap(localP).ifPresent(cap -> {
            float invisible = Math.max(0.15F, cap.getInvisibleValue(partialTicks));

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            float f = player.getAttackAnim(partialTicks);
            InteractionHand interactionhand = MoreObjects.firstNonNull(player.swingingArm, InteractionHand.MAIN_HAND);
            float f1 = Mth.lerp(partialTicks, player.xRotO, player.getXRot());
            ItemInHandRenderer.HandRenderSelection iteminhandrenderer$handrenderselection = evaluateWhichHandsToRender(player);
            float f2 = Mth.lerp(partialTicks, player.xBobO, player.xBob);
            float f3 = Mth.lerp(partialTicks, player.yBobO, player.yBob);
            poseStack.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(partialTicks) - f2) * 0.1F));
            poseStack.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(partialTicks) - f3) * 0.1F));
            assert iteminhandrenderer$handrenderselection != null;
            if (iteminhandrenderer$handrenderselection.renderMainHand) {
                float f4 = interactionhand == InteractionHand.MAIN_HAND ? f : 0.0F;
                float f5 = 1.0F - Mth.lerp(partialTicks, this.oMainHandHeight, this.mainHandHeight);
                if (!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(InteractionHand.MAIN_HAND, poseStack, mcBufferSource, light, partialTicks, f1, f4, f5, this.mainHandItem)) {
                    RenderSystem.setShaderColor(1F, 1F, 1F, invisible);
                    this.renderArmWithItem(player, partialTicks, f1, InteractionHand.MAIN_HAND, f4, this.mainHandItem, f5, poseStack, mcBufferSource, light);
                }
            }

            if (iteminhandrenderer$handrenderselection.renderOffHand) {
                float f6 = interactionhand == InteractionHand.OFF_HAND ? f : 0.0F;
                float f7 = 1.0F - Mth.lerp(partialTicks, this.oOffHandHeight, this.offHandHeight);
                if (!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(InteractionHand.OFF_HAND, poseStack, mcBufferSource, light, partialTicks, f1, f6, f7, this.offHandItem)) {
                    RenderSystem.setShaderColor(1F, 1F, 1F, invisible);
                    this.renderArmWithItem(player, partialTicks, f1, InteractionHand.OFF_HAND, f6, this.offHandItem, f7, poseStack, mcBufferSource, light);
                }
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            mcBufferSource.endBatch();
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        });
    }
}
