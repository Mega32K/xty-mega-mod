package com.mega.xty.common.network.s2c.warehouse;

import com.mega.xty.common.warehouse.WeaponWarehouseItems;
import com.mega.xty.common.warehouse.WeaponWarehouseSnapshot;
import com.mega.xty.proxy.ClientProxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenWeaponWarehousePacket {
    private final CompoundTag snapshotTag;

    public S2COpenWeaponWarehousePacket() {
        this(WeaponWarehouseItems.createDefaultSnapshot());
    }

    public S2COpenWeaponWarehousePacket(WeaponWarehouseSnapshot snapshot) {
        this.snapshotTag = snapshot.save();
    }

    private S2COpenWeaponWarehousePacket(CompoundTag snapshotTag) {
        this.snapshotTag = snapshotTag;
    }

    public static S2COpenWeaponWarehousePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2COpenWeaponWarehousePacket(friendlyByteBuf.readNbt());
    }

    public static void encode(S2COpenWeaponWarehousePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(packet.snapshotTag);
    }

    public static void handle(S2COpenWeaponWarehousePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2COpenWeaponWarehousePacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            WeaponWarehouseSnapshot snapshot = packet.snapshotTag == null
                    ? WeaponWarehouseItems.createDefaultSnapshot()
                    : WeaponWarehouseSnapshot.load(packet.snapshotTag);
            ClientProxy.openWeaponWarehouseScreen(snapshot);
        }
    }
}
