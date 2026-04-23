package com.mega.xty.mixin.tacz;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.xty.common.data.map1.ClientGame2Data;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.client.gameplay.LocalPlayerShoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@ModDependsMixin("tacz")
@Mixin(value = LocalPlayerShoot.class,remap = false)
public abstract class LocalPlayerShootMixin {
    @WrapOperation(method = "shoot", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/entity/IGunOperator;getSynSprintTime()F"))
    private float game2CanSprintingShoot(IGunOperator instance, Operation<Float> original) {
        if (ClientGame2Data.isStopped)
            return 0F;
        return original.call(instance);
    }
}
