package com.mega.map.mixin.tacz;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.tacz.guns.entity.TargetMinecart;
import com.tacz.guns.item.AmmoItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ModDependsMixin("tacz")
@Mixin(value = TargetMinecart.class)
public abstract class TargetMinecartMixin {
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof Player player) {
            if (!player.isCreative() && !source.isIndirect())
                cir.setReturnValue(true);
        }
    }
}
