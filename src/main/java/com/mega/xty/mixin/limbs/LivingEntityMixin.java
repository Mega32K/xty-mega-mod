package com.mega.xty.mixin.limbs;

import com.mega.xty.common.capability.Limbs;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    LivingEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void disableSwing(InteractionHand hand, boolean p_21013_, CallbackInfo ci) {
        if ((Object)this instanceof Player player) {
            CommonProxy.getXtyCap(player).ifPresent(cap-> {
                if (cap.isLimbDisabled(Limbs.LEFT_HAND) && hand == InteractionHand.OFF_HAND)
                    ci.cancel();
                else if (cap.isLimbDisabled(Limbs.RIGHT_HAND) && hand == InteractionHand.MAIN_HAND)
                    ci.cancel();
            });
        }
    }
}
