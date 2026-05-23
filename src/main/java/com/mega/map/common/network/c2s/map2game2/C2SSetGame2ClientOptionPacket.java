package com.mega.map.common.network.c2s.map2game2;

import com.mega.map.common.capability.FpsCapability;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSetGame2ClientOptionPacket {
    private final String key;
    private final boolean value;

    public C2SSetGame2ClientOptionPacket(String key, boolean value) {
        this.key = key;
        this.value = value;
    }

    public static C2SSetGame2ClientOptionPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SSetGame2ClientOptionPacket(friendlyByteBuf.readUtf(), friendlyByteBuf.readBoolean());
    }

    public static void encode(C2SSetGame2ClientOptionPacket packet, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(packet.key);
        friendlyByteBuf.writeBoolean(packet.value);
    }

    public static void handle(C2SSetGame2ClientOptionPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null) {
                handle0(packet, context);
            }
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SSetGame2ClientOptionPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) {
            return;
        }
        CommonProxy.getFPSCap(player).ifPresent(cap -> apply(cap, packet.key, packet.value));
    }

    private static void apply(FpsCapability capability, String key, boolean value) {
        switch (key) {
            case "hud.bombCountdownPrompt" -> capability.getGame2ClientOptions().hud().setShowBombCountdownPrompt(value);
            case "hud.bombProgressBar" -> capability.getGame2ClientOptions().hud().setShowBombProgressBar(value);
            case "hud.roundStartPrompt" -> capability.getGame2ClientOptions().hud().setShowRoundStartPrompt(value);
            case "hud.roundResultOverlay" -> capability.getGame2ClientOptions().hud().setShowRoundResultOverlay(value);
            case "hud.roundMvpOverlay" -> capability.getGame2ClientOptions().hud().setShowRoundMvpOverlay(value);
            case "visual.aspect43" -> capability.getGame2ClientOptions().visual().setUseAspect43(value);
            case "visual.roundStartPostEffect" -> capability.getGame2ClientOptions().visual().setUseRoundStartPostEffect(value);
            case "visual.deadPostEffect" -> capability.getGame2ClientOptions().visual().setUseDeadPostEffect(value);
            case "visual.deathCamera" -> capability.getGame2ClientOptions().visual().setUseDeathCamera(value);
            case "visual.c4SpectateCamera" -> capability.getGame2ClientOptions().visual().setUseC4SpectateCamera(value);
            case "audio.bombBeep" -> capability.getGame2ClientOptions().audio().setPlayBombBeep(value);
            case "audio.roundResultSound" -> capability.getGame2ClientOptions().audio().setPlayRoundResultSound(value);
            default -> {
            }
        }
    }
}
