package com.mega.xty.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AccessorAbstractArrow {
    @Invoker
    void callDoPostHurtEffects(LivingEntity l);
}
