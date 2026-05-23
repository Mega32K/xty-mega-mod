package com.mega.map.common.event.map1;

import com.mega.endinglib.client.ClientWrapped;
import com.mega.map.common.data.map1.ClientGame2Data;
import com.mega.map.common.data.map1.Game2SavedData;
import com.mega.map.proxy.CommonProxy;
import com.tacz.guns.api.event.common.GunFireEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Score;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Game2TaczEvents {
    @SubscribeEvent
    public static void onFire(GunFireEvent event) {
        if (event.getShooter() instanceof ServerPlayer player) {
            Game2SavedData savedData = Game2SavedData.getInstance(player.server);
            if (!savedData.isStopped()) {
                if (savedData.getHealthObjective() != null) {
                    Score score = player.server.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), savedData.getHealthObjective());
                    if (score.getScore() <= 0) {
                        event.setCanceled(true);
                        return;
                    }
                    if (!savedData.isSceneChanging()) {
                        String sF = savedData.getGame2Functions().getGunShootFunction();
                        if (sF != null && !sF.isEmpty()) {
                            player.server.getFunctions().get(ResourceLocation.parse(sF)).ifPresent(mF -> {
                                player.server.getFunctions().execute(mF, player.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2));
                            });
                        }
                    } else event.setCanceled(true);
                }
            }
        } else {
            if (!ClientGame2Data.isStopped) {
                if (ClientGame2Data.sceneChanging)
                    event.setCanceled(true);
                if (ClientWrapped.clientPlayer() != null) {
                    CommonProxy.getXtyCap(ClientWrapped.clientPlayer()).ifPresent(capability -> {
                        if (capability.getGame2Health() <= 0)
                            event.setCanceled(true);
                    });
                }
            }
        }
    }
}
