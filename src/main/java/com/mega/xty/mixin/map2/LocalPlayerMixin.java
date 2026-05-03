package com.mega.xty.mixin.map2;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    LocalPlayerMixin(ClientLevel p_250460_, GameProfile p_249912_) {
        super(p_250460_, p_249912_);
    }
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=0.2F"))
    private float noUsingSlowdown(float original) {
        if (ClientGameData.map2Playing()) return 1F;
        return original;
    }
}
