package com.mega.map.common.network.s2c.map2.game1;

import com.mega.map.common.data.map2.ClientGame1Data;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class S2CGame1EvolutionWeaponPacket {
    private final List<ItemStack> itemStacks;

    public S2CGame1EvolutionWeaponPacket(List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks;
    }

    public static S2CGame1EvolutionWeaponPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CGame1EvolutionWeaponPacket(friendlyByteBuf.readList(FriendlyByteBuf::readItem));
    }

    public static void encode(S2CGame1EvolutionWeaponPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeCollection(packet.itemStacks, FriendlyByteBuf::writeItem);
    }

    public static void handle(S2CGame1EvolutionWeaponPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CGame1EvolutionWeaponPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            synchronized (ClientGame1Data.evolutionWeapons) {
                ClientGame1Data.evolutionWeapons.clear();
                ClientGame1Data.evolutionWeapons.addAll(packet.itemStacks);
            }
        }
    }
}
