package com.mega.map.mixin.map1.game_2;

import com.mega.map.util.data_expand.ExtraPlayerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin implements ExtraPlayerRenderer {
    @Unique
    private boolean megaMod$isSlim;
    @Inject(method = "<init>", at = @At("TAIL"))
    private void setSlim(EntityRendererProvider.Context p_174557_, boolean p_174558_, CallbackInfo ci) {
        this.megaMod$isSlim = p_174558_;
    }

    @Override
    public boolean megaMod$isSlim() {
        return megaMod$isSlim;
    }

    @Override
    public void megaMod$setSlim(boolean slim) {
        megaMod$isSlim = slim;
    }
}
