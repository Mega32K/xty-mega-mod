package com.mega.map.mixin.gd656killicon;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.map.common.config.CommonConfig;
import com.mega.map.common.tags.XtyItemTags;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.mods.gd656killicon.Gd656killicon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ModDependsMixin("gd656killicon")
@Mixin(value = Gd656killicon.class, remap = false)
public abstract class Gd656killiconMixin {
    @Inject(method = "onLivingDeath", at = @At("HEAD"), cancellable = true)
    private void cancelNonTacz(LivingDeathEvent event, CallbackInfo ci) {
        if (!CommonConfig.killiconOnlyTacz) return;
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(itemStack.getItem() instanceof IGun) && !itemStack.is(XtyItemTags.HAS_GD656_KILL_ICON))
                ci.cancel();
        }
    }
    @Inject(method = "onLivingDamage", at = @At("HEAD"), cancellable = true)
    private void cancelNonTacz2(LivingDamageEvent event, CallbackInfo ci) {
        if (!CommonConfig.killiconOnlyTacz) return;
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(itemStack.getItem() instanceof IGun) && !itemStack.is(XtyItemTags.HAS_GD656_KILL_ICON))
                ci.cancel();
        }
    }
}
