package com.mega.xty.mixin.map2.game2;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mega.xty.common.init.ItemInit;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @WrapWithCondition(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private<T extends ParticleOptions>  boolean cancelFallingParticlesWhenGame2(ServerLevel instance, T j, double v, double p_8768_, double p_8769_, int p_8770_, double p_8771_, double p_8772_, double p_8773_, double p_8774_) {
        return !((Object)this instanceof Player player) || !player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get());
    }
    @WrapWithCondition(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playBlockFallSound()V"))
    private boolean cancelFallingBlockSoundsWhenGame2(LivingEntity instance) {
        return !(instance instanceof Player player) || !player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get());
    }

    @WrapWithCondition(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private boolean cancelFallingSoundsWhenGame2(LivingEntity instance, SoundEvent soundEvent, float v, float v2) {
        return !(instance instanceof Player player) || !player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get());
    }
}
