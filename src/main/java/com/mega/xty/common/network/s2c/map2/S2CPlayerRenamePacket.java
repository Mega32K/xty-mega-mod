package com.mega.xty.common.network.s2c.map2;

import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CPlayerRenamePacket {
    private final UUID id;

    public S2CPlayerRenamePacket(UUID id) {
        this.id = id;
    }

    public static S2CPlayerRenamePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CPlayerRenamePacket(friendlyByteBuf.readUUID());
    }

    public static void encode(S2CPlayerRenamePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(packet.id);
    }

    public static void handle(S2CPlayerRenamePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CPlayerRenamePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientProxy.openRenameScreen(packet.id);
        }
    }
}
