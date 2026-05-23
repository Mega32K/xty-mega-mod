package com.mega.map.mixin.lrtactical;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import me.xjqsh.lrtactical.item.consumable.ConsumableData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ModDependsMixin("lrtactical")
@Mixin(value = ConsumableData.class, remap = false)
public class ConsumableDataMixin {
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void getUseDuration(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1);
    }
}
