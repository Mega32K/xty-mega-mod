package com.mega.map.common.network.s2c.map2;

import com.mega.map.common.data.map2.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CPlayerCountNeedPacket {
    private final int countNeed;

    public S2CPlayerCountNeedPacket(int countNeed) {
        this.countNeed = countNeed;
    }

    public static S2CPlayerCountNeedPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CPlayerCountNeedPacket(friendlyByteBuf.readInt());
    }

    public static void encode(S2CPlayerCountNeedPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(packet.countNeed);
    }

    public static void handle(S2CPlayerCountNeedPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CPlayerCountNeedPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGameData.playerCountNeed = packet.countNeed;

        }
    }
}
