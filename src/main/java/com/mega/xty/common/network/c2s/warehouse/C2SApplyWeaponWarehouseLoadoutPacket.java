package com.mega.xty.common.network.c2s.warehouse;

import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.warehouse.S2CSyncWeaponWarehousePacket;
import com.mega.xty.common.warehouse.WeaponWarehouseItems;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SApplyWeaponWarehouseLoadoutPacket {
    private final int loadoutIndex;

    public C2SApplyWeaponWarehouseLoadoutPacket(int loadoutIndex) {
        this.loadoutIndex = loadoutIndex;
    }

    public static C2SApplyWeaponWarehouseLoadoutPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SApplyWeaponWarehouseLoadoutPacket(friendlyByteBuf.readVarInt());
    }

    public static void encode(C2SApplyWeaponWarehouseLoadoutPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(packet.loadoutIndex);
    }

    public static void handle(C2SApplyWeaponWarehouseLoadoutPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SApplyWeaponWarehouseLoadoutPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) {
            return;
        }
        CommonProxy.getWeaponWarehouseCap(player).ifPresent(cap -> {
            cap.setSelectedWarehouseLoadout(WeaponWarehouseItems.clampLoadoutIndex(packet.loadoutIndex));
            cap.applySelectedWarehouseLoadout(player);
            NetworkHandler.sendToPlayer(new S2CSyncWeaponWarehousePacket(cap.getWeaponWarehouse()), player);
        });
    }
}
