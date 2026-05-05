package com.mega.xty.common.network.c2s.map2;

import com.mega.endinglib.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class C2SSetNamePacket {
    private final String name;

    public C2SSetNamePacket(String name) {
        this.name = name;
    }

    public static C2SSetNamePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SSetNamePacket(friendlyByteBuf.readUtf());
    }

    public static void encode(C2SSetNamePacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(packet.name);
    }

    public static void handle(C2SSetNamePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SSetNamePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        CommonProxy.getCameraCapOptional(player).ifPresent(capability -> {
            if (packet.name.isEmpty())
                capability.setDisplayNameOpt(Optional.empty());
            else capability.setDisplayNameOpt(Optional.of(Component.literal(packet.name)));
        });
    }
}
