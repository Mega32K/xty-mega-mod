package com.mega.map.common.data.map2;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ClientGame1Data {
    public static int tickCount = 0;
    public static boolean isStopped = true;
    public static final ByteList evolutionSelector = new ByteArrayList();
    public static final List<ItemStack> evolutionWeapons = new ObjectArrayList<>();
    public static String formatTime() {
        StringBuilder builder = new StringBuilder(ClientGameData.countdownMin+":");
        if (ClientGameData.countdownSec < 10)
            builder.append('0').append(ClientGameData.countdownSec);
        else builder.append(ClientGameData.countdownSec);
        return builder.toString();
    }
    public static void tick(ClientLevel clientLevel) {
        if (!ClientGameData.isStopped && !isStopped) {
            tickCount++;
        } else {
            tickCount = 0;
        }
    }
    public static boolean playing() {
        return !ClientGameData.isStopped && !isStopped;
    }
}
