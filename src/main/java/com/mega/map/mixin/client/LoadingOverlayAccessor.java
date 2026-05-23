package com.mega.map.mixin.client;

import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LoadingOverlay.class)
public interface LoadingOverlayAccessor {
    @Accessor
    long getFadeOutStart();
    @Accessor
    long getFadeInStart();
    @Accessor
    void setFadeOutStart(long l);
    @Accessor
    void setFadeInStart(long l);
    @Accessor
    float getCurrentProgress();

}
