package com.mega.xty.common.network.c2s.map1.game2;

import com.mega.endinglib.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SPlayerLaydownPacket {

    public static C2SPlayerLaydownPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SPlayerLaydownPacket();
    }

    public static void encode(C2SPlayerLaydownPacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(C2SPlayerLaydownPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SPlayerLaydownPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        CommonProxy.getCameraCapOptional(player).ifPresent(capability -> capability.lockedPose(Pose.SWIMMING, 3, player));
    }
}
