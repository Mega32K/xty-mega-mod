package com.mega.xty.mixin.map1.game_2.scene_changing;

import com.mega.xty.common.data.map1.ClientGame2Data;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngeneMixin {

    @Inject(
            method = {"tick"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void tick(CallbackInfo ci) {
        if (ClientGame2Data.sceneChanging) {
            ci.cancel();
        }

    }

    @ModifyVariable(
            method = {"render*"},
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float render(float value) {
        return ClientGame2Data.sceneChanging ? 0.0F : value;
    }
}
