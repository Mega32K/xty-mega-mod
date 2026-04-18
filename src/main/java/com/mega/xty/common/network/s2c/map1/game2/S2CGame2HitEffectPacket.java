package com.mega.xty.common.network.s2c.map1.game2;

import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.xty.common.event.map1.Game2ClientEvents;
import com.mega.xty.common.particle.Game2HitParticleOption;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class S2CGame2HitEffectPacket {
    private final int entity;
    private final int target;

    public S2CGame2HitEffectPacket(int entity, int target) {
        this.entity = entity;
        this.target = target;
    }

    public static S2CGame2HitEffectPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CGame2HitEffectPacket(friendlyByteBuf.readVarInt(), friendlyByteBuf.readVarInt());
    }

    public static void encode(S2CGame2HitEffectPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(packet.entity);
        friendlyByteBuf.writeVarInt(packet.target);
    }

    public static void handle(S2CGame2HitEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CGame2HitEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            Entity e = ClientWrapped.clientLevel().getEntity(packet.entity);
            Entity t = ClientWrapped.clientLevel().getEntity(packet.target);
            if (e != null && t != null) {
                Vec3 v0 = e.position().add(0,e.getBbHeight()/2F,0);
                Vec3 v1 = t.position().add(0,t.getBbHeight()/2F,0);
                double distance = v0.distanceTo(v1);
                Matrix4f m = new Matrix4f(ClientUtils.LEVEL_PROJ_MAT).mul(ClientUtils.LEVEL_MODEL_VIEW_MAT);
                Vec2 tdV0 = Game2ClientEvents.transform(m, v0.toVector3f());
                Vec2 tdV1 = Game2ClientEvents.transform(m, v1.toVector3f());
                float roll = (float) (Math.atan2(tdV0.y - tdV1.y, tdV0.x - tdV1.x)) + Mth.HALF_PI;
                Vec3 center = new Vec3(
                        (v0.x + v1.x) / 2F,
                        (v0.y + v1.y) / 2F,
                        (v0.z + v1.z) / 2F
                );
                float count = (float) Math.max(20, 9 * distance);
                float half = count / 2F;
                for (int i=0;i<count;i++) {
                    float progress = Math.abs(i-half) / half * 1.3F - 0.15F;
                    Vec3 p = new Vec3(
                            Mth.lerp(progress, v0.x, v1.x),
                            Mth.lerp(progress, v0.y, v1.y),
                            Mth.lerp(progress, v0.z, v1.z)
                    );
                    Vec3 motion = p.add(center.scale(-1)).normalize().scale(0.1f);
                    for (int j=0;j<4;j++) {
                        double d = progress * 1.5F + 0.8F;
                        Vec3 pos = p.add((Math.random() * 0.4D - 0.2D) * d, (Math.random() * 0.5D - 0.25D) * d, (Math.random() * 0.4D - 0.2D) * d);
                        ClientWrapped.clientLevel().addParticle(new Game2HitParticleOption(roll), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);

                    }
                }
            }
        }
    }
}
