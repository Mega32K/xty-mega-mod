package com.mega.map.common.event.map2;

import com.mega.map.common.data.map2.Game1SavedData;
import com.mega.map.common.data.map2.Map2SavedData;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.s2c.map2.game1.*;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Game1CommonEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            Game1SavedData data = Game1SavedData.getInstance(server);
            NetworkHandler.sendToPlayer(new S2CGame1StatsPacket(data.isStopped()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CGame1EvolutionSelectorPacket(data.getOrPutPlayerSelector(serverPlayer.getUUID()).toByteArray()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CGame1EvolutionWeaponPacket(data.getSelectedWeapons(serverPlayer)), serverPlayer);
        }
    }
    @SubscribeEvent
    public static void addKillCount(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            Game1SavedData data = Game1SavedData.getInstance(serverPlayer.server);
            Map2SavedData map2SavedData = Map2SavedData.getInstance(serverPlayer.server);
            if (!data.isStopped() && !map2SavedData.isStopped()) {
                CommonProxy.getMap2Cap(serverPlayer).ifPresent(cap -> {
                    if (cap.getEvolutionKillCount() < 4) {
                        cap.setEvolutionKillCount(cap.getEvolutionKillCount() + 1);
                        if (cap.getEvolutionKillCount() >= 4) {
                            cap.setEvolutionIndex(Mth.clamp(cap.getEvolutionIndex() + 1, 0, 12));
                            cap.setEvolutionKillCount(0);
                        }
                    }
                });
            }
        }
    }
}
