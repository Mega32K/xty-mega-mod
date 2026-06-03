package com.mega.map.mixin.client;

import com.mega.endinglib.api.client.shader.post.PostProcessingShaders;
import com.mega.map.client.shader.post.fps.Aspect43PostEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void processAspect43AfterEntityOutline(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        Aspect43PostEffect effect = Aspect43PostEffect.INSTANCE;
        if (effect == null || PostProcessingShaders.isReloading || !effect.canUse()) {
            return;
        }

        PostChain postChain = effect.current();
        if (postChain == null) {
            return;
        }

        effect.onRenderTick(partialTicks);
        postChain.process(partialTicks);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }
}
