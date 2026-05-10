package com.mega.xty.common.network.s2c.map2.game2;

import com.mega.xty.client.shader.post.map2.Game2StartPostEffect;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CGame2StartEffectPacket {
    private final int durationTicks;

    public S2CGame2StartEffectPacket(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public static S2CGame2StartEffectPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CGame2StartEffectPacket(friendlyByteBuf.readVarInt());
    }

    public static void encode(S2CGame2StartEffectPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(packet.durationTicks);
    }

    public static void handle(S2CGame2StartEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CGame2StartEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGame2Data.setRoundStartLockedTicks(packet.durationTicks);
            if (Minecraft.getInstance().player != null && !CommonProxy.getFPSCap(Minecraft.getInstance().player).map(cap -> cap.getGame2ClientOptions().visual().useRoundStartPostEffect()).orElse(true)) {
                Game2StartPostEffect.stop();
                return;
            }
            if (packet.durationTicks > 0) {
                Game2StartPostEffect.start(packet.durationTicks);
            } else {
                Game2StartPostEffect.stop();
            }
        }
    }
}
