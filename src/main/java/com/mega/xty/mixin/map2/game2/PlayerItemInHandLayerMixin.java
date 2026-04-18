package com.mega.xty.mixin.map2.game2;

import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.SafeClass;
import com.mega.endinglib.util.time.TimeContext;
import com.mega.xty.common.capability.Map2Capability;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {
    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;

    @Shadow protected abstract void renderArmWithSpyglass(LivingEntity p_174518_, ItemStack p_174519_, HumanoidArm p_174520_, PoseStack p_174521_, MultiBufferSource p_174522_, int p_174523_);

    public PlayerItemInHandLayerMixin(RenderLayerParent<T, M> p_234846_, ItemInHandRenderer p_234847_) {
        super(p_234846_, p_234847_);
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void opticalNanoInvisibleStart(LivingEntity p_270884_, ItemStack p_270379_, ItemDisplayContext p_270607_, HumanoidArm p_270324_, PoseStack p_270124_, MultiBufferSource p_270414_, int p_270295_, CallbackInfo ci) {
        if (p_270884_ instanceof AbstractClientPlayer cp) {
            CommonProxy.getMap2Cap(cp).ifPresent(cap -> {
                float invisible = cap.getInvisibleValue(TimeContext.Client.alwaysPartial());
                if (invisible >= 0F) {
                    if (cp.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get())) {
                        Player localP = ClientWrapped.clientPlayer();
                        if (localP != null && (localP.isAlliedTo(cp) || localP == cp || localP.isSpectator()))
                            invisible = Math.max(0.15F, invisible);
                        if (invisible > 0F) {
                            float finalInvisible = invisible;
                            PoseStack p = new PoseStack();
                            p.setIdentity();
                            p.mulPoseMatrix(p_270124_.last().pose());
                            ClientGame2Data.addPostRenderedItemRender(p, (poseStack -> {
                                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                                RenderSystem.enableBlend();
                                RenderSystem.defaultBlendFunc();
                                RenderSystem.setShaderColor(1F, 1F, 1F, finalInvisible);
                                if (p_270379_.is(Items.SPYGLASS) && p_270884_.getUseItem() == p_270379_ && p_270884_.swingTime == 0) {
                                    this.renderArmWithSpyglass(p_270884_, p_270379_, p_270324_, poseStack, bufferSource, p_270295_);
                                } else {
                                    super.renderArmWithItem(p_270884_, p_270379_, p_270607_, p_270324_, poseStack, bufferSource, p_270295_);
                                }
                                RenderSystem.enableBlend();
                                RenderSystem.defaultBlendFunc();
                                bufferSource.endBatch();
                                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                            }));
                        }
                        ci.cancel();
                    }
                }
            });
        }
    }
}
