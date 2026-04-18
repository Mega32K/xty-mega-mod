package com.mega.xty.mixin.xaero;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.hud.minimap.player.tracker.PlayerTrackerIconRenderer;

@ModDependsMixin("xaerominimap")
@Mixin(value = PlayerTrackerIconRenderer.class)
public abstract class PlayerTrackerIconRendererMixin {
}
