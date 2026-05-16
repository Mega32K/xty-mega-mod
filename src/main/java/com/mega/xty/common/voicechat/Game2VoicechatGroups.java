package com.mega.xty.common.voicechat;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.common.data.map2.Map2SavedData;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class Game2VoicechatGroups {
    public static final String RED_GROUP_NAME = "RED";
    public static final String BLUE_GROUP_NAME = "BLUE";
    public static final String REFEREE_GROUP_NAME = "REFEREE";

    private static final UUID RED_GROUP_ID = UUID.fromString("a4dddff4-314f-4d30-9714-8f86fc6fb771");
    private static final UUID BLUE_GROUP_ID = UUID.fromString("293a5739-f0fc-4b16-a8fe-697f553f75b4");
    private static final UUID REFEREE_GROUP_ID = UUID.fromString("f6888db4-8b7f-4c84-9843-4f22d2ea6d3b");
    private static volatile VoicechatRoutes currentRoutes = VoicechatRoutes.empty();

    private Game2VoicechatGroups() {
    }

    public static void ensurePersistentGroups(@Nullable VoicechatServerApi api) {
        if (api == null) {
            return;
        }
        ensureGroup(api, RED_GROUP_ID, RED_GROUP_NAME, Group.Type.ISOLATED);
        ensureGroup(api, BLUE_GROUP_ID, BLUE_GROUP_NAME, Group.Type.ISOLATED);
        ensureGroup(api, REFEREE_GROUP_ID, REFEREE_GROUP_NAME, Group.Type.NORMAL);
    }

    public static boolean isGame2Playing(MinecraftServer server) {
        Map2SavedData map2SavedData = Map2SavedData.getInstance(server);
        if (map2SavedData.isStopped() || !map2SavedData.isTeamMode()) {
            return false;
        }
        return !Game2SavedData.getInstance(server).isStopped();
    }

    public static boolean isRefereePlayer(ServerPlayer player) {
        return player.isCreative() || player.isSpectator();
    }

    public static boolean isRefereeGroup(@Nullable Group group) {
        return group != null && REFEREE_GROUP_ID.equals(group.getId());
    }

    public static void clearAllPlayers(MinecraftServer server) {
        VoicechatServerApi api = Game2VoicechatPlugin.getServerApi();
        if (api == null) {
            currentRoutes = VoicechatRoutes.empty();
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            VoicechatConnection connection = api.getConnectionOf(player.getUUID());
            if (connection != null && connection.isInGroup()) {
                connection.setGroup(null);
            }
        }
        currentRoutes = VoicechatRoutes.empty();
    }

    public static void syncGame2Groups(MinecraftServer server) {
        VoicechatServerApi api = Game2VoicechatPlugin.getServerApi();
        if (api == null) {
            currentRoutes = VoicechatRoutes.empty();
            return;
        }
        ensurePersistentGroups(api);

        if (!isGame2Playing(server)) {
            clearAllPlayers(server);
            return;
        }

        Group redGroup = findGroup(api, RED_GROUP_ID);
        Group blueGroup = findGroup(api, BLUE_GROUP_ID);
        Group refereeGroup = findGroup(api, REFEREE_GROUP_ID);
        if (redGroup == null || blueGroup == null || refereeGroup == null) {
            XtyMegaMod.LOGGER.warn("Game2 voicechat groups are missing after initialization");
            currentRoutes = VoicechatRoutes.empty();
            return;
        }

        Map<UUID, VoicechatConnection> redConnections = new LinkedHashMap<>();
        Map<UUID, VoicechatConnection> blueConnections = new LinkedHashMap<>();
        Map<UUID, VoicechatConnection> refereeConnections = new LinkedHashMap<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            VoicechatConnection connection = api.getConnectionOf(player.getUUID());
            if (connection == null || !connection.isInstalled()) {
                continue;
            }

            Group targetGroup = getTargetGroup(player, redGroup, blueGroup, refereeGroup);
            if (targetGroup == null) {
                if (connection.isInGroup() && isManagedGroup(connection.getGroup())) {
                    connection.setGroup(null);
                }
                continue;
            }

            Group currentGroup = connection.getGroup();
            if (!sameGroup(currentGroup, targetGroup)) {
                connection.setGroup(targetGroup);
            }
            if (sameGroup(targetGroup, redGroup)) {
                redConnections.put(player.getUUID(), connection);
            } else if (sameGroup(targetGroup, blueGroup)) {
                blueConnections.put(player.getUUID(), connection);
            } else if (sameGroup(targetGroup, refereeGroup)) {
                refereeConnections.put(player.getUUID(), connection);
            }
        }
        currentRoutes = new VoicechatRoutes(redConnections, blueConnections, refereeConnections);
    }

    public static VoicechatRoutes getCurrentRoutes() {
        return currentRoutes;
    }

    @Nullable
    private static Group getTargetGroup(ServerPlayer player, Group redGroup, Group blueGroup, Group refereeGroup) {
        if (isRefereePlayer(player)) {
            return refereeGroup;
        }
        Team team = player.getTeam();
        if (team == null) {
            return null;
        }
        ChatFormatting color = team.getColor();
        if (color == ChatFormatting.RED) {
            return redGroup;
        }
        if (color == ChatFormatting.BLUE) {
            return blueGroup;
        }
        return null;
    }

    private static void ensureGroup(VoicechatServerApi api, UUID groupId, String name, Group.Type type) {
        if (findGroup(api, groupId) != null) {
            return;
        }
        api.groupBuilder()
                .setId(groupId)
                .setName(name)
                .setPersistent(true)
                .setHidden(false)
                .setType(type)
                .build();
    }

    @Nullable
    private static Group findGroup(VoicechatServerApi api, UUID groupId) {
        for (Group group : api.getGroups()) {
            if (groupId.equals(group.getId())) {
                return group;
            }
        }
        return null;
    }

    private static boolean sameGroup(@Nullable Group currentGroup, Group targetGroup) {
        return currentGroup != null && targetGroup.getId().equals(currentGroup.getId());
    }

    private static boolean isManagedGroup(@Nullable Group group) {
        if (group == null) {
            return false;
        }
        UUID id = group.getId();
        return RED_GROUP_ID.equals(id) || BLUE_GROUP_ID.equals(id) || REFEREE_GROUP_ID.equals(id);
    }

    public record VoicechatRoutes(
            Map<UUID, VoicechatConnection> redConnections,
            Map<UUID, VoicechatConnection> blueConnections,
            Map<UUID, VoicechatConnection> refereeConnections
    ) {
        private static VoicechatRoutes empty() {
            return new VoicechatRoutes(Map.of(), Map.of(), Map.of());
        }

        public VoicechatRoutes {
            redConnections = Collections.unmodifiableMap(new LinkedHashMap<>(redConnections));
            blueConnections = Collections.unmodifiableMap(new LinkedHashMap<>(blueConnections));
            refereeConnections = Collections.unmodifiableMap(new LinkedHashMap<>(refereeConnections));
        }

        public boolean isRedPlayer(UUID playerId) {
            return redConnections.containsKey(playerId);
        }

        public boolean isBluePlayer(UUID playerId) {
            return blueConnections.containsKey(playerId);
        }

        public boolean isRefereePlayer(UUID playerId) {
            return refereeConnections.containsKey(playerId);
        }
    }
}
