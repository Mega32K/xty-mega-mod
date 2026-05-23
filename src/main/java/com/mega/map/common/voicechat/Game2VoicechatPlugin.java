package com.mega.map.common.voicechat;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@ForgeVoicechatPlugin
public class Game2VoicechatPlugin implements VoicechatPlugin {
    private static volatile VoicechatServerApi serverApi;

    @Override
    public String getPluginId() {
        return "megamod_game2_voicechat";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi voicechatServerApi) {
            serverApi = voicechatServerApi;
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(PlayerConnectedEvent.class, this::onPlayerConnected);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();
        Game2VoicechatGroups.ensurePersistentGroups(serverApi);
    }

    private void onPlayerConnected(PlayerConnectedEvent event) {
        serverApi = event.getVoicechat();
        Game2VoicechatGroups.ensurePersistentGroups(serverApi);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) {
            return;
        }
        Object rawPlayer = senderConnection.getPlayer().getPlayer();
        if (!(rawPlayer instanceof ServerPlayer player)) {
            return;
        }
        if (!Game2VoicechatGroups.isGame2Playing(player.server)) {
            return;
        }

        var packet = event.getPacket().toStaticSoundPacket();
        UUID senderId = player.getUUID();
        Game2VoicechatGroups.VoicechatRoutes routes = Game2VoicechatGroups.getCurrentRoutes();
        if (routes.isRefereePlayer(senderId) || Game2VoicechatGroups.isRefereeGroup(senderConnection.getGroup())) {
            sendStaticSoundPacketTo(event, routes.redConnections(), senderId, packet);
            sendStaticSoundPacketTo(event, routes.blueConnections(), senderId, packet);
            return;
        }
        if (routes.isRedPlayer(senderId) || routes.isBluePlayer(senderId)) {
            sendStaticSoundPacketTo(event, routes.refereeConnections(), senderId, packet);
        }
    }

    private void sendStaticSoundPacketTo(MicrophonePacketEvent event, Map<UUID, VoicechatConnection> receivers, UUID senderId, StaticSoundPacket packet) {
        for (Map.Entry<UUID, VoicechatConnection> entry : receivers.entrySet()) {
            if (entry.getKey().equals(senderId)) {
                continue;
            }
            VoicechatConnection receiverConnection = entry.getValue();
            if (receiverConnection != null && receiverConnection.isInstalled()) {
                event.getVoicechat().sendStaticSoundPacketTo(receiverConnection, packet);
            }
        }
    }

    @Nullable
    public static VoicechatServerApi getServerApi() {
        return serverApi;
    }
}
