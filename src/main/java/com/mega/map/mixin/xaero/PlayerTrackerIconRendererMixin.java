package com.mega.map.mixin.xaero;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import org.spongepowered.asm.mixin.Mixin;
import xaero.hud.minimap.player.tracker.PlayerTrackerIconRenderer;

@ModDependsMixin("xaerominimap")
@Mixin(value = PlayerTrackerIconRenderer.class)
public abstract class PlayerTrackerIconRendererMixin {
}
