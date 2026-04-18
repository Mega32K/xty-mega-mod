package com.mega.xty.common.network.s2c.map2;

import com.mega.xty.common.data.map2.DeathData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CAddDeathDataPacket {
    private final Component killer;
    private final ItemStack itemStack;
    private final Component killed;

    public S2CAddDeathDataPacket(Component killer, ItemStack itemStack, Component killed) {
        this.killer = killer;
        this.itemStack = itemStack;
        this.killed = killed;
    }

    public static S2CAddDeathDataPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CAddDeathDataPacket(friendlyByteBuf.readComponent(), friendlyByteBuf.readItem(), friendlyByteBuf.readComponent());
    }

    public static void encode(S2CAddDeathDataPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeComponent(packet.killer);
        friendlyByteBuf.writeItem(packet.itemStack);
        friendlyByteBuf.writeComponent(packet.killed);
    }

    public static void handle(S2CAddDeathDataPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CAddDeathDataPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            new DeathData(packet.killer, packet.itemStack, packet.killed).add();
        }
    }
}
