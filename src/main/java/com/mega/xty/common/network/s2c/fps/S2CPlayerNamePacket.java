package com.mega.xty.common.network.s2c.fps;

import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.TabData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CPlayerNamePacket {
    /**
     * 玩家进入其他存档/其他服务器时为true
     */
    private final boolean differentSave;
    private final Map<UUID, TabData> dataMap;

    public S2CPlayerNamePacket(boolean differentSave, Map<UUID, TabData> dataMap) {
        this.differentSave = differentSave;
        this.dataMap = dataMap;
    }

    public static S2CPlayerNamePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CPlayerNamePacket(friendlyByteBuf.readBoolean(), friendlyByteBuf.readMap(FriendlyByteBuf::readUUID, TabData.F_READER));
    }

    public static void encode(S2CPlayerNamePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.differentSave);
        friendlyByteBuf.writeMap(packet.dataMap, FriendlyByteBuf::writeUUID, TabData.F_WRITER);
    }

    public static void handle(S2CPlayerNamePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CPlayerNamePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            if (packet.differentSave)
                ClientFpsData.playerDisplayNames.clear();
            for (var entry : packet.dataMap.entrySet())
                ClientFpsData.playerDisplayNames.put(entry.getKey(), new TabData(entry.getValue().isDead, entry.getValue().component.copy()));
        }
    }
}
