package com.mega.xty.common.network.s2c.map2;

import com.mega.xty.common.data.map2.ClientGameData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncPointsPacket {
    private final BlockPos a;
    private final BlockPos b;

    public S2CSyncPointsPacket(BlockPos a, BlockPos b) {
        if (a == null) a = new BlockPos(10000000, 1500, 10000000);
        if (b == null) b = new BlockPos(10000000, 1500, 10000000);
        this.a = a;
        this.b = b;
    }

    public static S2CSyncPointsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CSyncPointsPacket(friendlyByteBuf.readBlockPos(), friendlyByteBuf.readBlockPos());
    }

    public static void encode(S2CSyncPointsPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(packet.a);
        friendlyByteBuf.writeBlockPos(packet.b);
    }

    public static void handle(S2CSyncPointsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CSyncPointsPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGameData.pointA = packet.a.getY() > 1400 ? null : packet.a;
            ClientGameData.pointB = packet.b.getY() > 1400 ? null : packet.b;
        }
    }
}
