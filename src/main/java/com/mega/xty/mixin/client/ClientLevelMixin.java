package com.mega.xty.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.endinglib.util.annotation.DeprecatedMixin;
import com.mega.xty.common.init.BlockInit;
import com.mega.xty.common.init.ItemInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashSet;
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
