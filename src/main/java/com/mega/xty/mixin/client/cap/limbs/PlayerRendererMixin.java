package com.mega.xty.mixin.client.cap.limbs;

import com.mega.xty.common.capability.Limbs;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    PlayerRendererMixin(EntityRendererProvider.Context p_174289_, PlayerModel<AbstractClientPlayer> p_174290_, float p_174291_) {
        super(p_174289_, p_174290_, p_174291_);
    }

    @Inject(method = "setModelProperties", at = @At("RETURN"))
    private void setDisabledLimbProperties(AbstractClientPlayer player, CallbackInfo ci) {
        PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
        CommonProxy.getXtyCap(player).ifPresent(capability-> {
            if (playermodel.leftArm.visible) {
                playermodel.leftArm.visible = !capability.isLimbDisabled(Limbs.LEFT_HAND);
            }
            if (playermodel.leftSleeve.visible) {
                playermodel.leftSleeve.visible = !capability.isLimbDisabled(Limbs.LEFT_HAND);
            }
            if (playermodel.rightArm.visible) {
                playermodel.rightArm.visible = !capability.isLimbDisabled(Limbs.RIGHT_HAND);
            }
            if (playermodel.rightSleeve.visible) {
                playermodel.rightSleeve.visible = !capability.isLimbDisabled(Limbs.RIGHT_HAND);
            }
            if (playermodel.leftLeg.visible) {
                playermodel.leftLeg.visible = !capability.isLimbDisabled(Limbs.LEFT_LEG);
            }
            if (playermodel.leftPants.visible) {
                playermodel.leftPants.visible = !capability.isLimbDisabled(Limbs.LEFT_LEG);
            }
            if (playermodel.rightLeg.visible) {
                playermodel.rightLeg.visible = !capability.isLimbDisabled(Limbs.RIGHT_LEG);
            }
            if (playermodel.rightPants.visible) {
                playermodel.rightPants.visible = !capability.isLimbDisabled(Limbs.RIGHT_LEG);
            }
        });
    }
}
