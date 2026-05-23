package com.mega.map.common.network.s2c.map2;

import com.mega.map.common.data.map2.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CMap2StatsPacket {
    private final boolean stopped;

    public S2CMap2StatsPacket(boolean stopped) {
        this.stopped = stopped;
    }

    public static S2CMap2StatsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CMap2StatsPacket(friendlyByteBuf.readBoolean());
    }

    public static void encode(S2CMap2StatsPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.stopped);
    }

    public static void handle(S2CMap2StatsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CMap2StatsPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGameData.isStopped = packet.stopped;
            ClientGameData.blueTeamKillcount = ClientGameData.redTeamKillcount = 0;
        }
    }
}
