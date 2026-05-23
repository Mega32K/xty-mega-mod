package com.mega.map.common.network.s2c.fps;

import com.mega.map.common.data.fps.ClientFpsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRoundLoseRenderPacket {
    public S2CRoundLoseRenderPacket() {
    }

    public static S2CRoundLoseRenderPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CRoundLoseRenderPacket();
    }

    public static void encode(S2CRoundLoseRenderPacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(S2CRoundLoseRenderPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientFpsData.requestRoundLoseRender();
        }
    }
}
