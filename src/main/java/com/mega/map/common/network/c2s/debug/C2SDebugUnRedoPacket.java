package com.mega.map.common.network.c2s.debug;

import com.mega.map.common.item.IDebugItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SDebugUnRedoPacket {
    private final Item debugItem;
    private final boolean isUndo;

    public C2SDebugUnRedoPacket(Item debugItem, boolean isUndo) {
        this.debugItem = debugItem;
        this.isUndo = isUndo;
    }
    public static C2SDebugUnRedoPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SDebugUnRedoPacket(friendlyByteBuf.readById(BuiltInRegistries.ITEM), friendlyByteBuf.readBoolean());
    }

    public static void encode(C2SDebugUnRedoPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeId(BuiltInRegistries.ITEM, packet.debugItem);
        friendlyByteBuf.writeBoolean(packet.isUndo);
    }

    public static void handle(C2SDebugUnRedoPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SDebugUnRedoPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item = itemStack.getItem();
        if (item instanceof IDebugItem debugItem && item.getDescriptionId().equals(packet.debugItem.getDescriptionId())) {
            if (packet.isUndo) debugItem.debug().undo(player, itemStack);
            else debugItem.debug().redo(player, itemStack);
        }
    }
}
