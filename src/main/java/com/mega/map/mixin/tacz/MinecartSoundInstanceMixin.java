package com.mega.map.mixin.tacz;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.tacz.guns.entity.TargetMinecart;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ModDependsMixin("tacz")
@Mixin(MinecartSoundInstance.class)
public abstract class MinecartSoundInstanceMixin extends AbstractTickableSoundInstance {
    @Shadow @Final private AbstractMinecart minecart;

    @Shadow private float pitch;

    protected MinecartSoundInstanceMixin(SoundEvent p_235076_, SoundSource p_235077_, RandomSource p_235078_) {
        super(p_235076_, p_235077_, p_235078_);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void xty$silenceTargetMinecart(CallbackInfo ci) {
        if (this.minecart instanceof TargetMinecart) {
            this.volume = 0.0F;
            this.pitch = 0.0F;
        }
    }
}
