package com.mega.map.common.network.s2c.map2;

import com.mega.map.common.data.map2.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncTeamWinsPacket {
    private final int red;
    private final int blue;

    public S2CSyncTeamWinsPacket(int red, int blue) {
        this.red = red;
        this.blue = blue;
    }

    public static S2CSyncTeamWinsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CSyncTeamWinsPacket(friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
    }

    public static void encode(S2CSyncTeamWinsPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(packet.red);
        friendlyByteBuf.writeInt(packet.blue);
    }

    public static void handle(S2CSyncTeamWinsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CSyncTeamWinsPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGameData.redTeamScore = packet.red;
            ClientGameData.blueTeamScore = packet.blue;
        }
    }
}
