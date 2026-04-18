package com.mega.xty.common.network.s2c.map2.game2;

import com.mega.xty.common.data.map2.ClientGame1Data;
import com.mega.xty.common.data.map2.ClientGame2Data;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CGame2StatsPacket {
    private final boolean stopped;

    public S2CGame2StatsPacket(boolean stopped) {
        this.stopped = stopped;
    }

    public static S2CGame2StatsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CGame2StatsPacket(friendlyByteBuf.readBoolean());
    }

    public static void encode(S2CGame2StatsPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.stopped);
    }

    public static void handle(S2CGame2StatsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CGame2StatsPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGame2Data.isStopped = packet.stopped;
        }
    }
}
