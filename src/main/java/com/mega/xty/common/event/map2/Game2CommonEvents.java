package com.mega.xty.common.event.map2;

import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.game2.S2CGame2StatsPacket;
import com.mega.xty.common.network.s2c.map2.game2.S2CSyncGame2ServerOptionsPacket;
import com.mega.xty.common.network.s2c.map2.game2.S2CSyncGame2WarehouseMeleePacket;
import com.mega.xty.common.voicechat.Game2VoicechatGroups;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Game2CommonEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            Game2SavedData data = Game2SavedData.getInstance(server);
            NetworkHandler.sendToPlayer(new S2CGame2StatsPacket(data.isStopped()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CSyncGame2WarehouseMeleePacket(data.getExtraWarehouseMeleeStacks()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CSyncGame2ServerOptionsPacket(data.getServerOptions()), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Game2VoicechatGroups.syncGame2Groups(event.getServer());
        }
    }
}
