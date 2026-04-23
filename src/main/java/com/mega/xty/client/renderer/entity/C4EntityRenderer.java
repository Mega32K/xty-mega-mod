package com.mega.xty.client.renderer.entity;

import com.mega.xty.common.entity.C4Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

public class C4EntityRenderer extends EntityRenderer<C4Entity> {
    private final ItemRenderer itemRenderer;
    public C4EntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull C4Entity c4Entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(@NotNull C4Entity entity, float pEntityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int pPackedLight) {
        poseStack.mulPose(Axis.YP.rotation(entity.getYRot() * Mth.DEG_TO_RAD));
        poseStack.mulPose(Axis.XP.rotation((float)Math.PI * 0.5F));
        this.itemRenderer.renderStatic(entity.getC4(), ItemDisplayContext.FIXED, pPackedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), entity.getId());

    }
}
