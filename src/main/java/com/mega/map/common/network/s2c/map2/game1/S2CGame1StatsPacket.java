package com.mega.map.common.network.s2c.map2.game1;

import com.mega.map.common.data.map2.ClientGame1Data;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CGame1StatsPacket {
    private final boolean stopped;

    public S2CGame1StatsPacket(boolean stopped) {
        this.stopped = stopped;
    }

    public static S2CGame1StatsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CGame1StatsPacket(friendlyByteBuf.readBoolean());
    }

    public static void encode(S2CGame1StatsPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.stopped);
    }

    public static void handle(S2CGame1StatsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CGame1StatsPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGame1Data.isStopped = packet.stopped;
        }
    }
}
