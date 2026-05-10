package com.mega.xty.common.network.s2c.map2.game2;

import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.options.map2game2.Game2ServerOptions;
import com.mega.xty.common.options.map2game2.Game2ServerOptionsCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncGame2ServerOptionsPacket {
    private final CompoundTag optionsTag;

    public S2CSyncGame2ServerOptionsPacket(Game2ServerOptions options) {
        this.optionsTag = options.save(new CompoundTag());
    }

    private S2CSyncGame2ServerOptionsPacket(CompoundTag optionsTag) {
        this.optionsTag = optionsTag == null ? new CompoundTag() : optionsTag;
    }

    public static S2CSyncGame2ServerOptionsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CSyncGame2ServerOptionsPacket(friendlyByteBuf.readNbt());
    }

    public static void encode(S2CSyncGame2ServerOptionsPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(packet.optionsTag);
    }

    public static void handle(S2CSyncGame2ServerOptionsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CSyncGame2ServerOptionsPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientGame2Data.SERVER_OPTIONS.load(packet.optionsTag);
            Game2ServerOptionsCache.CURRENT.load(packet.optionsTag);
        }
    }
}
