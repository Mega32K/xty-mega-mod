package com.mega.map.client.overlay;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public interface DebugModule extends IGuiOverlay {
    default void onMouseButton(InputEvent.MouseButton.Post event) {}
    default void onKeyboard(InputEvent.Key event) {}
    default void onMouseScrolling(InputEvent.MouseScrollingEvent event) {}
    void tick();
    default void undo(ServerPlayer player, ItemStack stack) {}
    default void redo(ServerPlayer player, ItemStack stack) {}
}
