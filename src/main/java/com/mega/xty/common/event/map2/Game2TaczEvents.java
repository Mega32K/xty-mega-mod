package com.mega.xty.common.event.map2;

import com.mega.endinglib.client.ClientWrapped;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.capability.Map2Capability;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.proxy.CommonProxy;
import com.tacz.guns.api.event.common.GunFireEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableBoolean;

@Mod.EventBusSubscriber(modid = XtyMegaMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Game2TaczEvents {
    @SubscribeEvent
    public static void onFire(GunFireEvent event) {
        LivingEntity shooter = event.getShooter();
        if (shooter instanceof ServerPlayer player) {
            if (isRoundStartLocked(player.server, player) || isDeadLocked(player)) {
                event.setCanceled(true);
            }
            return;
        }
        if (shooter == ClientWrapped.clientPlayer() && isClientRoundStartLocked()) {
            event.setCanceled(true);
        }
    }

    private static boolean isRoundStartLocked(net.minecraft.server.MinecraftServer server, ServerPlayer player) {
        Game2SavedData game2SavedData = Game2SavedData.getInstance(server);
        if (game2SavedData.isStopped()) {
            return false;
        }
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            long unlockGameTime = cap.getRoundKeyboardUnlockGameTime();
            mutableBoolean.setValue(unlockGameTime >= 0L && server.overworld().getGameTime() < unlockGameTime);
        });
        return mutableBoolean.getValue();

    }

    private static boolean isDeadLocked(ServerPlayer player) {
        return CommonProxy.getMap2Cap(player).map(Map2Capability::isXaeroDead).orElse(false);
    }

    private static boolean isClientRoundStartLocked() {
        return ClientGame2Data.isRoundStartLocked();
    }
}
