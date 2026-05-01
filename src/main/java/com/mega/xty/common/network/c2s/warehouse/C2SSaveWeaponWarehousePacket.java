package com.mega.xty.common.network.c2s.warehouse;

import com.mega.xty.common.warehouse.WeaponWarehouseItems;
import com.mega.xty.common.warehouse.WeaponWarehouseSnapshot;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.warehouse.S2CSyncWeaponWarehousePacket;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSaveWeaponWarehousePacket {
    private final CompoundTag snapshotTag;
    private final boolean applySelectedLoadout;

    public C2SSaveWeaponWarehousePacket(WeaponWarehouseSnapshot snapshot, boolean applySelectedLoadout) {
        this.snapshotTag = snapshot.save();
        this.applySelectedLoadout = applySelectedLoadout;
    }

    private C2SSaveWeaponWarehousePacket(CompoundTag snapshotTag, boolean applySelectedLoadout) {
        this.snapshotTag = snapshotTag;
        this.applySelectedLoadout = applySelectedLoadout;
    }

    public static C2SSaveWeaponWarehousePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SSaveWeaponWarehousePacket(friendlyByteBuf.readNbt(), friendlyByteBuf.readBoolean());
    }

    public static void encode(C2SSaveWeaponWarehousePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(packet.snapshotTag);
        friendlyByteBuf.writeBoolean(packet.applySelectedLoadout);
    }

    public static void handle(C2SSaveWeaponWarehousePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SSaveWeaponWarehousePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) {
            return;
        }
        FpsSavedData fpsSavedData = FpsSavedData.getInstance(player.server);
        WeaponWarehouseSnapshot snapshot = WeaponWarehouseItems.sanitizeSnapshot(WeaponWarehouseSnapshot.load(packet.snapshotTag), fpsSavedData.getWarehouseGunBlacklist());
        CommonProxy.getWeaponWarehouseCap(player).ifPresent(cap -> {
            cap.setWeaponWarehouse(snapshot, fpsSavedData.getWarehouseGunBlacklist());
            if (packet.applySelectedLoadout) {
                cap.applySelectedWarehouseLoadout(player, fpsSavedData.getWarehouseGunBlacklist());
            }
            NetworkHandler.sendToPlayer(new S2CSyncWeaponWarehousePacket(cap.getWeaponWarehouse()), player);
        });
    }
}
