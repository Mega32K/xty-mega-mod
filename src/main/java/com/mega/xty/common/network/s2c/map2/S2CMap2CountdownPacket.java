package com.mega.xty.common.network.s2c.map2;

import com.mega.xty.common.data.map2.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CMap2CountdownPacket {
    private final int countdown;

    public S2CMap2CountdownPacket(int countdown) {
        this.countdown = countdown;
    }

    public static S2CMap2CountdownPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CMap2CountdownPacket(friendlyByteBuf.readInt());
    }

    public static void encode(S2CMap2CountdownPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(packet.countdown);
    }

    public static void handle(S2CMap2CountdownPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CMap2CountdownPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGameData.countdownMin = packet.countdown / 1200;
            ClientGameData.countdownSec = (packet.countdown - ClientGameData.countdownMin * 1200) / 20;
        }
    }
}
