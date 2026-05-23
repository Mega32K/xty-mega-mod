package com.mega.map.mixin.bettercombat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.map.common.data.map1.ClientGame2Data;
import com.mega.map.proxy.ClientProxy;
import net.bettercombat.client.ClientNetwork;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientNetwork.class)
public abstract class ClientNetworkMixin {
    @WrapOperation(method = "lambda$initializeHandlers$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private static void modifySweepingSound(ClientLevel instance, double x, double y, double z, SoundEvent se, SoundSource ss, float v, float p, boolean p_104607_, Operation<Void> original) {
        if (!ClientGame2Data.isStopped) {
            v *= 3F;
            ClientProxy.playSoundNoDelayed(x, y, z, se, ss, v, p, p_104607_, instance.random.nextLong());
            return;
        }
        original.call(instance, x, y, z, se, ss, v, p, p_104607_);
    }
}
