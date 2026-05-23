package com.mega.map.common.network.s2c.fps;

import com.mega.map.common.data.fps.ClientFpsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRoundWinRenderPacket {
    public S2CRoundWinRenderPacket() {
    }

    public static S2CRoundWinRenderPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CRoundWinRenderPacket();
    }

    public static void encode(S2CRoundWinRenderPacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(S2CRoundWinRenderPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientFpsData.requestRoundWinRender();
        }
    }
}
