package com.mega.xty.common.network;

import com.mega.endinglib.common.network.PacketHandler;
import com.mega.endinglib.mixin.accessor.AccessorChunkMap;
import com.mega.endinglib.mixin.accessor.AccessorTrackedEntity;
import com.mega.xty.common.network.c2s.debug.C2SDebugUnRedoPacket;
import com.mega.xty.common.network.c2s.debug.fill_fucntion.C2SClearRecordsPacket;
import com.mega.xty.common.network.c2s.debug.fill_fucntion.C2SFillMakeLinePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public class NetworkHandler {
    public static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath("xtymegamod", "xtymegamod_packet"), () -> "1", (s) -> true, (s) -> true);
    private static final AtomicInteger id = new AtomicInteger(0);
    private static int id() {
        return id.getAndIncrement();
    }
    public static void registerPackets() {
        INSTANCE.registerMessage(id(), C2SDebugUnRedoPacket.class, C2SDebugUnRedoPacket::encode, C2SDebugUnRedoPacket::decode, C2SDebugUnRedoPacket::handle);
        INSTANCE.registerMessage(id(), C2SFillMakeLinePacket.class, C2SFillMakeLinePacket::encode, C2SFillMakeLinePacket::decode, C2SFillMakeLinePacket::handle);
        INSTANCE.registerMessage(id(), C2SClearRecordsPacket.class, C2SClearRecordsPacket::encode, C2SClearRecordsPacket::decode, C2SClearRecordsPacket::handle);

    }
    public static <MSG> void sendToAll(MSG msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static <MSG> void sendToEntity(MSG message, LivingEntity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
}
