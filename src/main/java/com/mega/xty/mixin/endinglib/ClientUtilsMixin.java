package com.mega.xty.mixin.endinglib;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.xty.common.data.map1.ClientGame2Data;
import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientUtils.class, remap = false)
public abstract class ClientUtilsMixin {
    @WrapOperation(method = "lambda$mouseCF$34", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/ClipContext$Block;OUTLINE:Lnet/minecraft/world/level/ClipContext$Block;"))
    private static ClipContext.Block modifyGame2ClipContext(Operation<ClipContext.Block> original) {
        if (!ClientGame2Data.isStopped)
            return ClipContext.Block.COLLIDER;
        return original.call();
    }
}
