package com.mega.map.client.overlay;

import com.mega.endinglib.client.ClientWrapped;
import com.mega.map.common.item.IDebugItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public record DebugOverlays(String name) implements IGuiOverlay {
    public static final DebugOverlays INSTANCE = new DebugOverlays("");
    public static int tickCount;
    public DebugOverlays(String name) {
        this.name = name;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Player player = ClientWrapped.clientPlayer();
        if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IDebugItem debugItem) {
            debugItem.debug().render(gui, guiGraphics, partialTick, screenWidth, screenHeight);
        }
    }
    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Player player = ClientWrapped.clientPlayer();
            if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IDebugItem debugItem) {
                debugItem.debug().tick();
                tickCount++;
            } else
                tickCount = 0;
        }
    }
    @SubscribeEvent
    public void inputEvent(InputEvent event) {
        if (Minecraft.getInstance().screen != null) return;
        Player player = ClientWrapped.clientPlayer();
        if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IDebugItem debugItem) {
            if (event instanceof InputEvent.Key k) {
                debugItem.debug().onKeyboard(k);
            } else if (event instanceof InputEvent.MouseButton.Post mb) {
                debugItem.debug().onMouseButton(mb);
            } else if (event instanceof InputEvent.MouseScrollingEvent ms) {
                debugItem.debug().onMouseScrolling(ms);
            }
        }
    }
}
