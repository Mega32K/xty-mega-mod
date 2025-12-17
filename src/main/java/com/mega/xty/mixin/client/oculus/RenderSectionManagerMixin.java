package com.mega.xty.mixin.client.oculus;

import com.mega.xty.proxy.ClientProxy;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager"}, remap = false)
public abstract class RenderSectionManagerMixin {
    @Inject(method = "isSectionVisible", at = @At("RETURN"), cancellable = true)
    private void onSectionVisibleReturn(int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (!ClientProxy.isUpdatingNoCullingInfo) {
                if (!ClientProxy.chunksNoCullingBlocks.isEmpty()) {
                    for (BlockPos bp : ClientProxy.chunksNoCullingBlocks) {
                        if (x == bp.getX() && z == bp.getZ()) {
                            cir.setReturnValue((Math.abs(bp.getY() - y) <= 4));
                        }
                    }
                }
            }
        }
    }
}
