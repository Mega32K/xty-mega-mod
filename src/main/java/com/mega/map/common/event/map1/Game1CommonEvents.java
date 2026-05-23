package com.mega.map.common.event.map1;

import com.mega.map.common.data.map1.ClientGame2Data;
import com.mega.map.common.data.map1.Game2SavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Game1CommonEvents {
    @SubscribeEvent
    public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().getMainHandItem().isEmpty()) {
            if (event.getEntity().level().isClientSide) {
                if (ClientGame2Data.disableBlockInteraction)
                    event.setCanceled(true);
            } else {
                if (event.getEntity().level() instanceof ServerLevel serverLevel) {
                    if (Game2SavedData.getInstance(serverLevel.getServer()).isDisableBlockInteraction())
                        event.setCanceled(true);
                }
            }
        }
    }
}
