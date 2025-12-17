package com.mega.xty.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.xty.proxy.ClientProxy;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @WrapOperation(method = "applyFrustum", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB chunkNoCullingCheck(ChunkRenderDispatcher.RenderChunk instance, Operation<AABB> original) {
        AABB aabb = original.call(instance);
        if (!ClientProxy.isUpdatingNoCullingInfo) {
            if (!ClientProxy.chunksNoCullingBlocks2.isEmpty()) {
                Vec3 c = aabb.getCenter();
                if (ClientProxy.chunksNoCullingBlocks2.contains(new BlockPos(SectionPos.blockToSectionCoord(c.x), SectionPos.blockToSectionCoord(c.y), SectionPos.blockToSectionCoord(c.z))))
                    return new AABB(c, c).inflate(512+aabb.getSize()*2.0);
            }
        }
        return aabb;
    }
}
