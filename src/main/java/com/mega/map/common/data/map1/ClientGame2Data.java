package com.mega.map.common.data.map1;

import com.mega.endinglib.client.ClientWrapped;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

public class ClientGame2Data {
    public static boolean disableBlockInteraction = false;
    public static boolean isStopped = true;
    public static boolean sceneChanging = true;
    public static float lastHealth;
    public static long blackAppearTime;
    public static long blackDisappearTime;
    public static boolean blackHoleExist;

    public static void tick(ClientLevel clientLevel) {
        Player player = ClientWrapped.clientPlayer();
        if (clientLevel != null && player != null) {
            String pName = player.getScoreboardName();
            if (ClientGame2Data.isStopped) return;
            CommonProxy.getXtyCap(player).ifPresent(capability -> {
                lastHealth = capability.getGame2Health();
            });
        }
    }
}
