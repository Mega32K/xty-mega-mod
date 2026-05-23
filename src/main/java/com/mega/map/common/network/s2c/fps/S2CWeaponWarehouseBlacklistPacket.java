package com.mega.map.common.network.s2c.fps;

import com.mega.map.common.data.fps.ClientFpsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class S2CWeaponWarehouseBlacklistPacket {
    private final Set<ResourceLocation> blacklistedGuns;

    public S2CWeaponWarehouseBlacklistPacket(Set<ResourceLocation> blacklistedGuns) {
        this.blacklistedGuns = new LinkedHashSet<>(blacklistedGuns);
    }

    public static S2CWeaponWarehouseBlacklistPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CWeaponWarehouseBlacklistPacket(friendlyByteBuf.readCollection(LinkedHashSet::new, FriendlyByteBuf::readResourceLocation));
    }

    public static void encode(S2CWeaponWarehouseBlacklistPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeCollection(packet.blacklistedGuns, FriendlyByteBuf::writeResourceLocation);
    }

    public static void handle(S2CWeaponWarehouseBlacklistPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CWeaponWarehouseBlacklistPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientFpsData.setWarehouseGunBlacklist(packet.blacklistedGuns);
        }
    }
}
