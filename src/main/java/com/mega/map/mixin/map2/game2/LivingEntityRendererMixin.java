package com.mega.map.mixin.map2.game2;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.map.common.capability.Map2Capability;
import com.mega.map.common.init.ItemInit;
import com.mega.map.proxy.CommonProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private <T extends LivingEntity, M extends EntityModel<T>> void render(T p_115308_, float p_115309_, float p_115310_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_, CallbackInfo ci, @Share("entityCall")LocalRef<Entity> entityLocalRef, @Share("partialTicks")LocalRef<Float> floatLocalRef) {
        entityLocalRef.set(p_115308_);
        floatLocalRef.set(p_115310_);
    }
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private <T extends LivingEntity, M extends EntityModel<T>> void opticalNanoInvisible(M instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i2, float r, float g, float b, float a, Operation<Void> original, @Share("entityCall")LocalRef<Entity> entityLocalRef, @Share("partialTicks")LocalRef<Float> floatLocalRef) {
        if (entityLocalRef.get() instanceof AbstractClientPlayer cp) {
            Map2Capability cap = CommonProxy.getMap2Cap(cp).orElse(null);
            if (cap != null) {
                float invisible = cap.getInvisibleValue(floatLocalRef.get());
                if (invisible >= 0F) {
                    if (cp.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get())
                            /*&& (cp.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && cp.getItemInHand(InteractionHand.OFF_HAND).isEmpty())*/) {
                        Player localP = ClientWrapped.clientPlayer();
                        if (localP != null && (localP.isAlliedTo(cp) || localP == cp || localP.isSpectator()))
                            invisible = Math.max(0.15F, invisible);
                        if (invisible <= 1.0F) {
                            a *= invisible;
                        }
                    }
                }
            }
        }
        original.call(instance, poseStack, vertexConsumer, i, i2, r, g, b, a);
    }
}
