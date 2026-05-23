package com.mega.map.common.network.s2c.map2.game2;

import com.mega.map.common.data.map2.ClientGame2Data;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class S2CSyncGame2WarehouseMeleePacket {
    private final List<ItemStack> meleeStacks;

    public S2CSyncGame2WarehouseMeleePacket(List<ItemStack> meleeStacks) {
        this.meleeStacks = meleeStacks.stream().map(ItemStack::copy).toList();
    }

    public static S2CSyncGame2WarehouseMeleePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CSyncGame2WarehouseMeleePacket(friendlyByteBuf.readList(FriendlyByteBuf::readItem));
    }

    public static void encode(S2CSyncGame2WarehouseMeleePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeCollection(packet.meleeStacks, FriendlyByteBuf::writeItem);
    }

    public static void handle(S2CSyncGame2WarehouseMeleePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CSyncGame2WarehouseMeleePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGame2Data.extraWarehouseMeleeStacks = new ObjectArrayList<>(packet.meleeStacks.stream().map(ItemStack::copy).toList());
        }
    }
}
