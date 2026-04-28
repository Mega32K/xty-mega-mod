package com.mega.xty.common.network.s2c.warehouse;

import com.mega.xty.proxy.ClientProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenWeaponWarehousePacket {
    public S2COpenWeaponWarehousePacket() {
    }

    public static S2COpenWeaponWarehousePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2COpenWeaponWarehousePacket();
    }

    public static void encode(S2COpenWeaponWarehousePacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(S2COpenWeaponWarehousePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ClientProxy.openWeaponWarehouseScreen();
        }
    }
}
