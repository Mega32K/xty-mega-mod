package com.mega.map.mixin.tacz;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.tacz.guns.item.AmmoItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ModDependsMixin("tacz")
@Mixin(value = AmmoItem.class)
public abstract class AmmoItemMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true, remap = false)
    private void xty$setAmmoMaxStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(99);
    }
}
