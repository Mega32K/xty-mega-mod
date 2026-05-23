package com.mega.map.common.network.s2c.fps;

import com.mega.map.common.data.fps.ClientFpsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CBombDataPacket {
    private final boolean bombExist;
    private final byte bombPos;
    private final int bombCountdownTicks;

    public S2CBombDataPacket(boolean bombExist, byte bombPos, int bombCountdownTicks) {
        this.bombExist = bombExist;
        this.bombPos = bombPos;
        this.bombCountdownTicks = bombCountdownTicks;
    }

    public static S2CBombDataPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CBombDataPacket(friendlyByteBuf.readBoolean(), friendlyByteBuf.readByte(), friendlyByteBuf.readInt());
    }

    public static void encode(S2CBombDataPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.bombExist);
        friendlyByteBuf.writeByte(packet.bombPos);
        friendlyByteBuf.writeInt(packet.bombCountdownTicks);
    }

    public static void handle(S2CBombDataPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CBombDataPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientFpsData.setBombData(packet.bombExist, packet.bombPos, packet.bombCountdownTicks);
        }
    }
}
