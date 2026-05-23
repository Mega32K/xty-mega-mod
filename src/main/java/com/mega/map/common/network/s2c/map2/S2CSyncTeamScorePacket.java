package com.mega.map.common.network.s2c.map2;

import com.mega.map.common.data.map2.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncTeamScorePacket {
    private final int red;
    private final int blue;

    public S2CSyncTeamScorePacket(int red, int blue) {
        this.red = red;
        this.blue = blue;
    }

    public static S2CSyncTeamScorePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CSyncTeamScorePacket(friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
    }

    public static void encode(S2CSyncTeamScorePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(packet.red);
        friendlyByteBuf.writeInt(packet.blue);
    }

    public static void handle(S2CSyncTeamScorePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CSyncTeamScorePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGameData.redTeamKillcount = packet.red;
            ClientGameData.blueTeamKillcount = packet.blue;
        }
    }
}
