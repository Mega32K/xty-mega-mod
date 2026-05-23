package com.mega.map.common.network.s2c.map1.game2;

import com.mega.map.common.data.map1.ClientGame2Data;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CGame2StatsPacket {
    private final boolean stopped;
    private final boolean sceneChanging;

    public S2CGame2StatsPacket(boolean stopped, boolean sceneChanging) {
        this.stopped = stopped;
        this.sceneChanging = sceneChanging;
    }

    public static S2CGame2StatsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CGame2StatsPacket(friendlyByteBuf.readBoolean(), friendlyByteBuf.readBoolean());
    }

    public static void encode(S2CGame2StatsPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.stopped);
        friendlyByteBuf.writeBoolean(packet.sceneChanging);
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
            ClientGame2Data.sceneChanging = packet.sceneChanging;
        }
    }
}
