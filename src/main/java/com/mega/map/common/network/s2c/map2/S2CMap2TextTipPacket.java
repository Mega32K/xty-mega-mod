package com.mega.map.common.network.s2c.map2;

import com.mega.map.common.data.map2.ClientGameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class S2CMap2TextTipPacket {
    private final boolean notNull;
    private final @Nullable Component rightTopText;
    public S2CMap2TextTipPacket(boolean notNull, @Nullable Component rightTopText) {
        this.notNull = notNull;
        this.rightTopText = rightTopText;
    }

    public static S2CMap2TextTipPacket decode(FriendlyByteBuf friendlyByteBuf) {
        boolean z = friendlyByteBuf.readBoolean();
        return new S2CMap2TextTipPacket(z, z ? friendlyByteBuf.readComponent() : null);
    }

    public static void encode(S2CMap2TextTipPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.notNull);
        if (packet.rightTopText != null)
            friendlyByteBuf.writeComponent(packet.rightTopText);
    }

    public static void handle(S2CMap2TextTipPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CMap2TextTipPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGameData.rightTopTextTip = packet.notNull ? packet.rightTopText : null;
        }
    }
}
