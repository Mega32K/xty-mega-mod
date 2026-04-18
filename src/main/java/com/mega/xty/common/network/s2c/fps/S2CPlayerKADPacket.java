package com.mega.xty.common.network.s2c.fps;

import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.kad.SynchedKADData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CPlayerKADPacket {
    /**
     * 玩家进入其他存档/其他服务器时为true
     */
    private final boolean differentSave;
    private final Map<UUID, SynchedKADData> dataMap;

    public S2CPlayerKADPacket(boolean differentSave, Map<UUID, SynchedKADData> dataMap) {
        this.differentSave = differentSave;
        this.dataMap = dataMap;
    }

    public static S2CPlayerKADPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CPlayerKADPacket(friendlyByteBuf.readBoolean(), friendlyByteBuf.readMap(FriendlyByteBuf::readUUID, SynchedKADData.F_READER));
    }

    public static void encode(S2CPlayerKADPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.differentSave);
        friendlyByteBuf.writeMap(packet.dataMap, FriendlyByteBuf::writeUUID, SynchedKADData.F_WRITER);
    }

    public static void handle(S2CPlayerKADPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CPlayerKADPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            if (packet.differentSave)
                ClientFpsData.kadData.clear();
            ClientFpsData.kadData.putAll(packet.dataMap);
        }
    }
}
