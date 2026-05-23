package com.mega.map.mixin.map2.game2;

import com.mega.map.common.data.map2.ClientGame2Data;
import com.mega.map.common.data.map2.ClientGameData;
import com.mega.map.common.init.ItemInit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void renderHitbox(PoseStack p_114442_, VertexConsumer p_114443_, Entity e, float p_114445_, CallbackInfo ci) {
        if (e instanceof AbstractClientPlayer cp) {
            if (ClientGameData.map2Playing() && ClientGame2Data.playing())
                if (cp.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get()))
                    ci.cancel();
        }
    }
}
