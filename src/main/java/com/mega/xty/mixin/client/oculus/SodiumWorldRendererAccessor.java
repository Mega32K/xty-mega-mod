package com.mega.xty.mixin.client.oculus;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = {"me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer"}, remap = false)
public interface SodiumWorldRendererAccessor {
    @Accessor
    RenderSectionManager getRenderSectionManager();
}
