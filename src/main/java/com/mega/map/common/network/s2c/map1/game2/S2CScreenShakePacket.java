package com.mega.map.common.network.s2c.map1.game2;

import com.mega.map.game2.ShakeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class S2CScreenShakePacket {
    private final Vector3f start;
    private final Vector3f end;
    private final float power;

    public S2CScreenShakePacket(Vector3f start, Vector3f end, float power) {
        this.start = start;
        this.end = end;
        this.power = power;
    }

    public static S2CScreenShakePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CScreenShakePacket(friendlyByteBuf.readVector3f(), friendlyByteBuf.readVector3f(), friendlyByteBuf.readFloat());
    }

    public static void encode(S2CScreenShakePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVector3f(packet.start);
        friendlyByteBuf.writeVector3f(packet.end);
        friendlyByteBuf.writeFloat(packet.power);
    }

    public static void handle(S2CScreenShakePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CScreenShakePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ShakeHandler.handleScreenShake(packet);
        }
    }

    public Vector3f getStart() {
        return start;
    }

    public Vector3f getEnd() {
        return end;
    }

    public float getPower() {
        return power;
    }
}
