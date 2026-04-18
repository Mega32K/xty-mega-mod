package com.mega.xty.common.network.c2s.map2;

import com.mega.xty.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SStopJoiningGamePacket {

    public static C2SStopJoiningGamePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SStopJoiningGamePacket();
    }

    public static void encode(C2SStopJoiningGamePacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(C2SStopJoiningGamePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SStopJoiningGamePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            cap.setNeedStart(false);
        });
    }
}
