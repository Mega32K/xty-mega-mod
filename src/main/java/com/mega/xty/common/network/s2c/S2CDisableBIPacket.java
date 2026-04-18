package com.mega.xty.common.network.s2c;

import com.mega.xty.common.data.map1.ClientGame2Data;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CDisableBIPacket {
    private final boolean disable;

    public S2CDisableBIPacket(boolean disable) {
        this.disable = disable;
    }

    public static S2CDisableBIPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CDisableBIPacket(friendlyByteBuf.readBoolean());
    }

    public static void encode(S2CDisableBIPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.disable);
    }

    public static void handle(S2CDisableBIPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CDisableBIPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGame2Data.disableBlockInteraction = packet.disable;
        }
    }
}
