package com.mega.xty.client.oculus;

import com.mega.xty.mixin.client.oculus.SodiumWorldRendererAccessor;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.world.WorldRendererExtended;
import me.jellysquid.mods.sodium.mixin.core.render.world.WorldRendererMixin;
import net.minecraft.client.Minecraft;

public class OculusSafeClass {
    public static void onSectionAdded(int x, int y, int z) {
        renderSectionManager().onSectionAdded(x, y, z);
    }
    public static RenderSectionManager renderSectionManager() {
        return ((SodiumWorldRendererAccessor)((WorldRendererExtended) Minecraft.getInstance().levelRenderer).sodium$getWorldRenderer()).getRenderSectionManager();
    }
}
