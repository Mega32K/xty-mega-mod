package com.mega.xty.common.network.s2c.map2;

import com.mega.xty.common.data.map2.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CMap2TeamScoreVisiblePacket {
    private final boolean visible;

    public S2CMap2TeamScoreVisiblePacket(boolean stopped) {
        this.visible = stopped;
    }

    public static S2CMap2TeamScoreVisiblePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CMap2TeamScoreVisiblePacket(friendlyByteBuf.readBoolean());
    }

    public static void encode(S2CMap2TeamScoreVisiblePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.visible);
    }

    public static void handle(S2CMap2TeamScoreVisiblePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CMap2TeamScoreVisiblePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGameData.teamScoreVisible = packet.visible;
        }
    }
}
