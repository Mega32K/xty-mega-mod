package com.mega.xty.common.voicechat;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@ForgeVoicechatPlugin
public class Game2VoicechatPlugin implements VoicechatPlugin {
    private static volatile VoicechatServerApi serverApi;

    @Override
    public String getPluginId() {
        return "xtymegamod_game2_voicechat";
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
        boolean senderIsReferee = Game2VoicechatGroups.isRefereePlayer(player) || Game2VoicechatGroups.isRefereeGroup(senderConnection.getGroup());
        for (ServerPlayer serverPlayer : player.server.getPlayerList().getPlayers()) {
            if (serverPlayer.getUUID().equals(player.getUUID())) {
                continue;
            }

            boolean receiverIsReferee = Game2VoicechatGroups.isRefereePlayer(serverPlayer);
            if (senderIsReferee == receiverIsReferee) {
                continue;
            }

            VoicechatConnection receiverConnection = event.getVoicechat().getConnectionOf(serverPlayer.getUUID());
            if (receiverConnection == null || !receiverConnection.isInstalled()) {
                continue;
            }
            event.getVoicechat().sendStaticSoundPacketTo(receiverConnection, packet);
        }
    }

    @Nullable
    public static VoicechatServerApi getServerApi() {
        return serverApi;
    }
}
