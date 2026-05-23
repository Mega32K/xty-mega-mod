package com.mega.map.common.network.s2c.fps;

import com.mega.map.common.data.fps.RoundStartData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRoundStartRenderPacket {
    public S2CRoundStartRenderPacket() {
    }

    public static S2CRoundStartRenderPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CRoundStartRenderPacket();
    }

    public static void encode(S2CRoundStartRenderPacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(S2CRoundStartRenderPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            RoundStartData.requestRoundStartRender();
        }
    }
}
