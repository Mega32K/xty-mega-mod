package com.mega.map.mixin.tacz;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@ModDependsMixin("tacz")
@Mixin(value = EntityKineticBullet.class)
public abstract class EntityKineticBulletMixin {
    @WrapWithCondition(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
    private boolean igniteEntity(Entity e, int seconds) {
        if (e.getRemainingFireTicks() < 10)
            e.setRemainingFireTicks(10);
        return false;
    }
}
