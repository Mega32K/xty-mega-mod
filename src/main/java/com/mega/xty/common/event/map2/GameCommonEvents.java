package com.mega.xty.common.event.map2;

import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.data.map2.ServerGameData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.*;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GameCommonEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            Map2SavedData data = Map2SavedData.getInstance(server);
            NetworkHandler.sendToPlayer(new S2CMap2StatsPacket(data.isStopped()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CPlayerCountNeedPacket(data.getPlayerCountNeed()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CMap2ModePacket(data.isTeamMode()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CSyncTeamScorePacket(data.getRedScore(), data.getBlueScore()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CMap2CountdownPacket(data.getCountdown()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CMap2TeamScoreVisiblePacket(data.isTeamScoreVisible()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CMap2ScoreOverlayVisiblePacket(data.isScoreOverlayVisible()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CSyncPointsPacket(data.getPointA(), data.getPointB()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CMap2TextTipPacket(data.getRightTopText() != null, data.getRightTopText()), serverPlayer);
        }
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Map2SavedData data = Map2SavedData.getInstance(event.getServer());
            if (!data.isStopped()) {
                MinecraftServer server = event.getServer();
                FpsSavedData fpsSavedData = FpsSavedData.getInstance(server);
                if (!Game2SavedData.getInstance(server).isStopped() && fpsSavedData.isBombExist()) {
                    if (data.getCountdown() != 0) {
                        data.setCountdown(0);
                        NetworkHandler.sendToAll(new S2CMap2CountdownPacket(data.getCountdown()));
                    }
                } else if (data.getCountdown() > 0) {
                    data.setCountdown(data.getCountdown() - 1);
                    if (data.getCountdown() % 20 == 0) {
                        NetworkHandler.sendToAll(new S2CMap2CountdownPacket(data.getCountdown()));
                    }
                    if (data.getCountdown() == 0) {
                        String functionS = data.getMap2Functions().getCountdownStopFunction();
                        if (functionS != null && !functionS.isEmpty()) {
                            if (!server.getPlayerList().getPlayers().isEmpty()) {
                                ServerPlayer player = server.getPlayerList().getPlayers().get(0);
                                server.getFunctions().get(ResourceLocation.parse(functionS)).ifPresent(commandFunction -> server.getFunctions().execute(commandFunction, player.createCommandSourceStack().withMaximumPermission(2).withSuppressedOutput()));
                            }
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void map2PlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (event.player instanceof ServerPlayer player) {
                MinecraftServer server = player.server;
                if (ServerGameData.map2Playing(server)) {
                    player.getFoodData().setFoodLevel(20);
                    checkLockingPos(player);
                }
            } else if (ClientGameData.map2Playing()) {
                checkLockingPos(event.player);
            }
        }
    }
    public static void checkLockingPos(Player player) {
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            if (cap.isXaeroDead()) {
                Vec3 lockPos = cap.getPlayerC4Pos()
                        .map(Vec3::new)
                        .or(() -> cap.getLastDeathPos().map(Vec3::new))
                        .orElse(null);
                if (lockPos != null) {
                    player.xOld = lockPos.x;
                    player.yOld = lockPos.y;
                    player.zOld = lockPos.z;
                    player.setPos(lockPos);
                }
            }
        });
    }
}
