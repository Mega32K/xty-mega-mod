package com.mega.xty.common.network.s2c.fps;

import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.kad.SynchedKADData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CBombDataPacket {
    private final boolean bombExist;
    private final byte bombPos;

    public S2CBombDataPacket(boolean bombExist, byte bombPos) {
        this.bombExist = bombExist;
        this.bombPos = bombPos;
    }

    public static S2CBombDataPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CBombDataPacket(friendlyByteBuf.readBoolean(), friendlyByteBuf.readByte());
    }

    public static void encode(S2CBombDataPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.bombExist);
        friendlyByteBuf.writeByte(packet.bombPos);
    }

    public static void handle(S2CBombDataPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CBombDataPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientFpsData.bombExist = packet.bombExist;
            ClientFpsData.bombPosition = packet.bombPos <= 0 ? "" : packet.bombPos == 1 ? "A点" : "B点";
        }
    }
}
