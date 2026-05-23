package com.mega.map.mixin.endinglib;

import com.mega.endinglib.mixin.accessor.AccessorEntity;
import com.mega.endinglib.util.mixin.data_expand.ExtraEntityData;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExtraEntityData.class, remap = false)
public abstract class ExtraEntityDataMixin {
    @Shadow @Final private Entity entity;

    @Shadow private int tickCount;

    @Inject(method = "forceTick", at = @At("HEAD"))
    private void forceTick(CallbackInfo ci) {
        if (this.entity instanceof Player player) {
            CommonProxy.getXtyCap(player).ifPresent(capability -> {
                capability.canUsePartialTeleportAnim = capability.smoothStartPos != null;
                if (capability.smoothStartPos != null)
                    if (tickCount - capability.smoothTeleportStart > capability.smoothTeleportDuration) {
                        capability.smoothStartPos = null;
                        capability.canUsePartialTeleportAnim = false;
                        player.noPhysics = false;
                        player.xOld = capability.smoothTeleportTarget.x;
                        player.yOld = capability.smoothTeleportTarget.y;
                        player.zOld = capability.smoothTeleportTarget.z;
                        player.setPos(capability.smoothTeleportTarget);
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.server.getCommands().performPrefixedCommand(serverPlayer.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2), "tp %s %s %s".formatted(capability.smoothTeleportTarget.x, capability.smoothTeleportTarget.y, capability.smoothTeleportTarget.z));
                        }
                    } else {
                        if (player.level().isClientSide) {
                            double d0 = Mth.lerp(capability.calculateInterpolationProgress(0.5F), capability.smoothStartPos.x, capability.smoothTeleportTarget.x);
                            double d1 = Mth.lerp(capability.calculateInterpolationProgress(0.5F), capability.smoothStartPos.y, capability.smoothTeleportTarget.y);
                            double d2 = Mth.lerp(capability.calculateInterpolationProgress(0.5F), capability.smoothStartPos.z, capability.smoothTeleportTarget.z);

                            player.xOld = d0;
                            player.yOld = d1;
                            player.zOld = d2;
                            ((AccessorEntity) player).setPositionField(new Vec3(d0, d1, d2));
                        }
                    }
            });
        }
    }
}
