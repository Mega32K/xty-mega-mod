package com.mega.map.common.network.s2c;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.mixin.data_expand.ExtraEntity;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CPartialTeleportPacket {
    private final UUID id;
    private final Easing easing;
    private final int duration;
    private final Vector3f pos;

    public S2CPartialTeleportPacket(UUID id, Easing easing, int duration, Vector3f pos) {
        this.id = id;
        this.easing = easing;
        this.duration = duration;
        this.pos = pos;
    }

    public static S2CPartialTeleportPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CPartialTeleportPacket(friendlyByteBuf.readUUID(), friendlyByteBuf.readEnum(Easing.class), friendlyByteBuf.readInt(), friendlyByteBuf.readVector3f());
    }

    public static void encode(S2CPartialTeleportPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(packet.id);
        friendlyByteBuf.writeEnum(packet.easing);
        friendlyByteBuf.writeInt(packet.duration);
        friendlyByteBuf.writeVector3f(packet.pos);
    }

    public static void handle(S2CPartialTeleportPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CPartialTeleportPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            Player player = ClientWrapped.clientLevel().getPlayerByUUID(packet.id);
            if (player != null) {
                CommonProxy.getXtyCap(player).ifPresent(capability -> {
                    capability.smoothStartPos = new Vec3(player.xOld, player.yOld, player.zOld);
                    capability.smoothTeleportTarget = new Vec3(packet.pos);
                    capability.smoothTeleportStart = ExtraEntity.of(player).endinglib$getExtraEntityData().tickCount;
                    capability.smoothTeleportDuration = packet.duration;
                    capability.smoothTeleportEasing = packet.easing;
                    capability.canUsePartialTeleportAnim = true;
                });
            }
        }
    }
}
