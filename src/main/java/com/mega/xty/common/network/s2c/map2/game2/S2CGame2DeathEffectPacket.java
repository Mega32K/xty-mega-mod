package com.mega.xty.common.network.s2c.map2.game2;

import com.mega.xty.common.event.map2.DeathCameraEffectHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CGame2DeathEffectPacket {
    private final Vec3 start;
    private final Vec3 end;
    private final float xRot;
    private final float yRot;

    public S2CGame2DeathEffectPacket(Vec3 start, Vec3 end, float xRot, float yRot) {
        this.start = start;
        this.end = end;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public static S2CGame2DeathEffectPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CGame2DeathEffectPacket(
                new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()),
                new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()),
                friendlyByteBuf.readFloat(),
                friendlyByteBuf.readFloat()
        );
    }

    public static void encode(S2CGame2DeathEffectPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeDouble(packet.start.x);
        friendlyByteBuf.writeDouble(packet.start.y);
        friendlyByteBuf.writeDouble(packet.start.z);
        friendlyByteBuf.writeDouble(packet.end.x);
        friendlyByteBuf.writeDouble(packet.end.y);
        friendlyByteBuf.writeDouble(packet.end.z);
        friendlyByteBuf.writeFloat(packet.xRot);
        friendlyByteBuf.writeFloat(packet.yRot);
    }

    public static void handle(S2CGame2DeathEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CGame2DeathEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            DeathCameraEffectHandler.play(packet.start, packet.end, packet.xRot, packet.yRot);
        }
    }
}
