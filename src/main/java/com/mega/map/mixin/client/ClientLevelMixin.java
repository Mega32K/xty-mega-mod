package com.mega.map.mixin.client;

import com.mega.endinglib.util.annotation.DeprecatedMixin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ClientLevel.class)
@DeprecatedMixin
public abstract class ClientLevelMixin {
    @Mutable
    @Shadow @Final private static Set<Item> MARKER_PARTICLE_ITEMS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void extraMarkerItems(CallbackInfo ci) {
    }
}
