package com.mega.xty.mixin.map1.game_2;

import com.llamalad7.mixinextras.sugar.Local;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow private Level level;

    @Inject(
            method = "move",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;horizontalCollision:Z", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER)
    )
    private void captureCollide(MoverType p_19973_, Vec3 p_19974_, CallbackInfo ci, @Local(ordinal = 1) Vec3 collide) {
        if ((Object)this instanceof Player player) {
            CommonProxy.getXtyCap(player).ifPresent(cap -> {
                cap.xlCollision = !Mth.equal(p_19974_.x, collide.x);
            });
        }
    }
}
