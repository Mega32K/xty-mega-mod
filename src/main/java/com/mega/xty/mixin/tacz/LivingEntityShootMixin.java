package com.mega.xty.mixin.tacz;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.xty.common.data.map1.ClientGame2Data;
import com.mega.xty.common.data.map1.Game2SavedData;
import com.tacz.guns.entity.shooter.LivingEntityShoot;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@ModDependsMixin("tacz")
@Mixin(value = LivingEntityShoot.class,remap = false)
public abstract class LivingEntityShootMixin {
    @Shadow @Final private LivingEntity shooter;

    @WrapOperation(method = "shoot", at = @At(value = "FIELD", target = "Lcom/tacz/guns/entity/shooter/ShooterDataHolder;sprintTimeS:F"))
    private float game2CanSprintingShoot(ShooterDataHolder instance, Operation<Float> original) {
        if (this.shooter.level().isClientSide) {
            if (!ClientGame2Data.isStopped) return 0F;
        } else {
            if (this.shooter.level() instanceof ServerLevel serverLevel)
                if (!Game2SavedData.getInstance(serverLevel.getServer()).isStopped()) return 0F;
        }
        return original.call(instance);
    }
}
