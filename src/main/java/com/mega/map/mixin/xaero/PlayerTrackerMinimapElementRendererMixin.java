package com.mega.map.mixin.xaero;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.map.proxy.ClientProxy;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.hud.minimap.player.tracker.PlayerTrackerMinimapElementRenderer;

@Mixin(PlayerTrackerMinimapElementRenderer.class)
@ModDependsMixin("xaerominimap")
public abstract class PlayerTrackerMinimapElementRendererMixin {
    @Inject(method = "getPlayerSkin", at = @At("HEAD"), cancellable = true, remap = false)
    private void getPlayerSkin(Player player, PlayerInfo info, CallbackInfoReturnable<ResourceLocation> cir) {
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            if (cap.isXaeroDead())
                cir.setReturnValue(ClientProxy.DEATH_PLAYER_SKIN);
        });
    }
    @WrapOperation(
            method = "renderElement(Lxaero/hud/minimap/player/tracker/PlayerTrackerMinimapElement;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Z",
            at = @At(value = "INVOKE", target = "Lxaero/hud/entity/EntityUtils;getEntityX(Lnet/minecraft/world/entity/Entity;F)D", remap = false),
            remap = false
    )
    private double getX(Entity e, float partial, Operation<Double> original) {
        if (e instanceof Player p) {
            MutableDouble value = new MutableDouble(original.call(e, partial));
            CommonProxy.getMap2Cap(p).ifPresent(cap -> cap.getLastDeathPos().ifPresent(pos -> value.setValue(pos.x)));
            return value.getValue();
        } else {
            return original.call(e, partial);
        }
    }
    @WrapOperation(
            method = "renderElement(Lxaero/hud/minimap/player/tracker/PlayerTrackerMinimapElement;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Z",
            at = @At(value = "INVOKE", target = "Lxaero/hud/entity/EntityUtils;getEntityX(Lnet/minecraft/world/entity/Entity;F)D", remap = false),
            remap = false
    )
    private double getY(Entity e, float partial, Operation<Double> original) {
        if (e instanceof Player p) {
            MutableDouble value = new MutableDouble(original.call(e, partial));
            CommonProxy.getMap2Cap(p).ifPresent(cap -> cap.getLastDeathPos().ifPresent(pos -> value.setValue(pos.y)));
            return value.getValue();
        } else {
            return original.call(e, partial);
        }
    }
    @WrapOperation(
            method = "renderElement(Lxaero/hud/minimap/player/tracker/PlayerTrackerMinimapElement;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Z",
            at = @At(value = "INVOKE", target = "Lxaero/hud/entity/EntityUtils;getEntityX(Lnet/minecraft/world/entity/Entity;F)D", remap = false),
            remap = false
    )
    private double getZ(Entity e, float partial, Operation<Double> original) {
        if (e instanceof Player p) {
            MutableDouble value = new MutableDouble(original.call(e, partial));
            CommonProxy.getMap2Cap(p).ifPresent(cap -> cap.getLastDeathPos().ifPresent(pos -> value.setValue(pos.z)));
            return value.getValue();
        } else {
            return original.call(e, partial);
        }
    }
}
