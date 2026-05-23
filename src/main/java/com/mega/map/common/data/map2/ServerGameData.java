package com.mega.map.common.data.map2;

import net.minecraft.server.MinecraftServer;

public class ServerGameData {
    public static boolean map2Playing(MinecraftServer server) {
        if (Map2SavedData.getInstance(server).isStopped()) return false;
        return !Game1SavedData.getInstance(server).isStopped() || !Game2SavedData.getInstance(server).isStopped();
    }
}
