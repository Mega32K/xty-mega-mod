package com.mega.xty.common.network.s2c.map2.game1;

import com.mega.xty.common.data.map2.ClientGame1Data;
import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class S2CGame1EvolutionSelectorPacket {
    private final byte[] selector;

    public S2CGame1EvolutionSelectorPacket(byte[] selector) {
        this.selector = selector;
    }

    public static S2CGame1EvolutionSelectorPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CGame1EvolutionSelectorPacket(friendlyByteBuf.readByteArray());
    }

    public static void encode(S2CGame1EvolutionSelectorPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByteArray(packet.selector);
    }

    public static void handle(S2CGame1EvolutionSelectorPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CGame1EvolutionSelectorPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            synchronized (ClientGame1Data.evolutionSelector) {
                ClientGame1Data.evolutionSelector.clear();
                for (byte b : packet.selector)
                    ClientGame1Data.evolutionSelector.add(b);
            }
        }
    }
}
