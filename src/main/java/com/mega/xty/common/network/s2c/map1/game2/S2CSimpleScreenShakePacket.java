package com.mega.xty.common.network.s2c.map1.game2;

import com.mega.xty.game2.ShakeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSimpleScreenShakePacket {
    private final float x;
    private final float y;
    private final float power;

    public S2CSimpleScreenShakePacket(float x, float y, float power) {
        this.x = x;
        this.y = y;
        this.power = power;
    }
    public static S2CSimpleScreenShakePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CSimpleScreenShakePacket(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
    }

    public static void encode(S2CSimpleScreenShakePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(packet.x);
        friendlyByteBuf.writeFloat(packet.y);
        friendlyByteBuf.writeFloat(packet.power);
    }

    public static void handle(S2CSimpleScreenShakePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CSimpleScreenShakePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ShakeHandler.handleSimpleScreenShake(packet);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPower() {
        return power;
    }
}
