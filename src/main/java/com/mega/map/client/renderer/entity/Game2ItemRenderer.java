package com.mega.map.client.renderer.entity;

import com.mega.endinglib.util.SafeClass;
import com.mega.map.common.data.map1.ClientGame2Data;
import com.mega.map.common.entity.Game2ItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class Game2ItemRenderer extends ItemEntityRenderer {
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();
    public Game2ItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ItemEntity e, float p_115037_, float p_115038_, PoseStack p_115039_, MultiBufferSource p_115040_, int p_115041_) {
        if (e instanceof Game2ItemEntity itemEntity) {
            if (SafeClass.usingShaderPack()) {
                p_115041_ = 15728880;
            }
            if (ClientGame2Data.sceneChanging)
                p_115038_ = 0F;
            p_115039_.pushPose();
            ItemStack itemstack = itemEntity.getItem();
            int i = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
            this.random.setSeed((long) i);
            BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, itemEntity.level(), (LivingEntity) null, itemEntity.getId());
            boolean flag = bakedmodel.isGui3d();
            int j = this.getRenderAmount(itemstack);
            float f = 0.25F;
            float f1 = shouldBob() ? Mth.sin(((float) itemEntity.getAge() + p_115038_) / 10.0F + itemEntity.bobOffs) * 0.1F + 0.1F : 0;
            float f2 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
            p_115039_.translate(0.0F, f1 + 0.25F * f2, 0.0F);
            float f3 = itemEntity.getSpin(p_115038_);
            Quaternionf rotation = new Quaternionf();
            rotation.rotateYXZ(f3 * itemEntity.ySpeed, f3 * itemEntity.xSpeed, f3 * itemEntity.zSpeed);
            p_115039_.mulPose(rotation);
            if (!flag) {
                float f7 = -0.0F * (float) (j - 1) * 0.5F;
                float f8 = -0.0F * (float) (j - 1) * 0.5F;
                float f9 = -0.09375F * (float) (j - 1) * 0.5F;
                p_115039_.translate(f7, f8, f9);
            }

            for (int k = 0; k < j; ++k) {
                p_115039_.pushPose();
                if (k > 0) {
                    if (flag) {
                        float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        p_115039_.translate(shouldSpreadItems() ? f11 : 0, shouldSpreadItems() ? f13 : 0, shouldSpreadItems() ? f10 : 0);
                    } else {
                        float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        p_115039_.translate(shouldSpreadItems() ? f12 : 0, shouldSpreadItems() ? f14 : 0, 0.0D);
                    }
                }

                this.itemRenderer.render(itemstack, ItemDisplayContext.GROUND, false, p_115039_, p_115040_, p_115041_, OverlayTexture.NO_OVERLAY, bakedmodel);
                p_115039_.popPose();
                if (!flag) {
                    p_115039_.translate(0.0, 0.0, 0.09375F);
                }
            }

            p_115039_.popPose();
             
        }
    }
}
