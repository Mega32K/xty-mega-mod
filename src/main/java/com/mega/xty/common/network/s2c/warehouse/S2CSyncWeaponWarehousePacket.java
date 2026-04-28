package com.mega.xty.common.network.s2c.warehouse;

import com.mega.xty.client.screen.warehouse.WeaponWarehouseScreen;
import com.mega.xty.common.warehouse.WeaponWarehouseItems;
import com.mega.xty.common.warehouse.WeaponWarehouseSnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncWeaponWarehousePacket {
    private final CompoundTag snapshotTag;

    public S2CSyncWeaponWarehousePacket(WeaponWarehouseSnapshot snapshot) {
        this.snapshotTag = snapshot.save();
    }

    private S2CSyncWeaponWarehousePacket(CompoundTag snapshotTag) {
        this.snapshotTag = snapshotTag;
    }

    public static S2CSyncWeaponWarehousePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CSyncWeaponWarehousePacket(friendlyByteBuf.readNbt());
    }

    public static void encode(S2CSyncWeaponWarehousePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(packet.snapshotTag);
    }

    public static void handle(S2CSyncWeaponWarehousePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CSyncWeaponWarehousePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            WeaponWarehouseSnapshot snapshot = WeaponWarehouseItems.sanitizeSnapshot(WeaponWarehouseSnapshot.load(packet.snapshotTag));
            WeaponWarehouseScreen.refreshOpenScreen(snapshot);
        }
    }
}
