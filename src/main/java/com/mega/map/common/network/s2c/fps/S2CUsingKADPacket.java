package com.mega.map.common.network.s2c.fps;

import com.mega.map.common.data.fps.ClientFpsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CUsingKADPacket {
    private final boolean enable;

    public S2CUsingKADPacket(boolean v) {
        this.enable = v;
    }

    public static S2CUsingKADPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CUsingKADPacket(friendlyByteBuf.readBoolean());
    }

    public static void encode(S2CUsingKADPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.enable);
    }

    public static void handle(S2CUsingKADPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CUsingKADPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientFpsData.enabled = packet.enable;
        }
    }
}
