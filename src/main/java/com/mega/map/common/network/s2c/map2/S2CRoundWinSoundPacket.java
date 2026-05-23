package com.mega.map.common.network.s2c.map2;

import com.mega.map.common.init.SoundsInit;
import com.mega.map.proxy.ClientProxy;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRoundWinSoundPacket {
    private final boolean redWin;

    public S2CRoundWinSoundPacket(boolean redWin) {
        this.redWin = redWin;
    }

    public static S2CRoundWinSoundPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CRoundWinSoundPacket(friendlyByteBuf.readBoolean());
    }

    public static void encode(S2CRoundWinSoundPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(packet.redWin);
    }

    public static void handle(S2CRoundWinSoundPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(S2CRoundWinSoundPacket packet, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            if (Minecraft.getInstance().player != null && !CommonProxy.getFPSCap(Minecraft.getInstance().player).map(cap -> cap.getGame2ClientOptions().audio().playRoundResultSound()).orElse(true)) {
                return;
            }
            SoundEvent sound = packet.redWin ? SoundsInit.TERWIN.get() : SoundsInit.CTWIN.get();
            ClientProxy.playSoundAtCamera(sound, 1.0F, 1.0F, Minecraft.getInstance().level != null ? Minecraft.getInstance().level.random.nextLong() : 0L);
        }
    }
}
