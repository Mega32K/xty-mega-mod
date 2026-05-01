package com.mega.xty.common.network.s2c.map2.game2;

import com.mega.xty.client.shader.post.map2.DeadPostEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CDeadPostEffectPacket {
    private final int durationTicks;

    public S2CDeadPostEffectPacket(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public static S2CDeadPostEffectPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CDeadPostEffectPacket(friendlyByteBuf.readVarInt());
    }

    public static void encode(S2CDeadPostEffectPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(packet.durationTicks);
    }

    public static void handle(S2CDeadPostEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CDeadPostEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            if (packet.durationTicks > 0) {
                DeadPostEffect.start(packet.durationTicks);
            } else {
                DeadPostEffect.stop();
            }
        }
    }
}
