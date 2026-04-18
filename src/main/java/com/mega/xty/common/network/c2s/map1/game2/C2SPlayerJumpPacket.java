package com.mega.xty.common.network.c2s.map1.game2;

import com.mega.xty.common.data.map1.Game2SavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SPlayerJumpPacket {

    public static C2SPlayerJumpPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SPlayerJumpPacket();
    }

    public static void encode(C2SPlayerJumpPacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(C2SPlayerJumpPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SPlayerJumpPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        MinecraftServer server = player.server;
        Game2SavedData savedData = Game2SavedData.getInstance(server);
        if (savedData.getGame2Functions().getJumpFunction() != null) {
            server.getFunctions().get(ResourceLocation.parse(savedData.getGame2Functions().getJumpFunction())).ifPresent(f -> {
                CommandSourceStack sourceStack = player.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2);
                server.getFunctions().execute(f, sourceStack);
            });
        }
    }
}
