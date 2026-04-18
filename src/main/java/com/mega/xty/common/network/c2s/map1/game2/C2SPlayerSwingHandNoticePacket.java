package com.mega.xty.common.network.c2s.map1.game2;

import com.mega.endinglib.common.command.argument.VanillaAnimation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SPlayerSwingHandNoticePacket {

    public static C2SPlayerSwingHandNoticePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SPlayerSwingHandNoticePacket();
    }

    public static void encode(C2SPlayerSwingHandNoticePacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(C2SPlayerSwingHandNoticePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SPlayerSwingHandNoticePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        MinecraftServer server = player.server;
        ClientboundAnimatePacket clientboundanimatepacket = new ClientboundAnimatePacket(player, VanillaAnimation.SWING_OFF_HAND.id);
        ServerChunkCache serverchunkcache = player.serverLevel().getChunkSource();
        serverchunkcache.broadcastAndSend(player, clientboundanimatepacket);
    }
}
