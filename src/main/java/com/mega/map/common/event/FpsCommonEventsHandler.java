package com.mega.map.common.event;

import com.mega.map.MegaMod;
import com.mega.map.common.data.fps.FpsSavedData;
import com.mega.map.common.data.fps.kad.KAD;
import com.mega.map.common.data.fps.kad.ServerSynchedKADData;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.s2c.fps.S2CBombDataPacket;
import com.mega.map.common.network.s2c.fps.S2CPlayerKADPacket;
import com.mega.map.common.network.s2c.fps.S2CPlayerNamePacket;
import com.mega.map.common.network.s2c.fps.S2CWeaponWarehouseBlacklistPacket;
import com.mega.map.common.network.s2c.fps.S2CUsingKADPacket;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableObject;

@Mod.EventBusSubscriber(modid = MegaMod.MODID)
public class FpsCommonEventsHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            FpsSavedData data = FpsSavedData.getInstance(server);
            data.setPlayerTab(serverPlayer);
            NetworkHandler.sendToPlayer(new S2CUsingKADPacket(data.isEnableKAD()), serverPlayer);
            if (data.isEnableKAD()) {
                NetworkHandler.sendToPlayer(new S2CPlayerKADPacket(true, data.getKadData()), serverPlayer);
            }
            NetworkHandler.sendToPlayer(new S2CWeaponWarehouseBlacklistPacket(data.getWarehouseGunBlacklist()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CBombDataPacket(data.isBombExist(), data.getBombPosition(), data.getBombCountdownTicks()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CPlayerNamePacket(true, data.getPlayerTabData()), serverPlayer);
        }
    }
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FpsSavedData.getInstance(serverPlayer.server).removePlayerTab(serverPlayer);
        }
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            FpsSavedData.getInstance(event.getServer()).tickBombCountdown();
        }
    }
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() instanceof ServerPlayer sourceP) {
            FpsSavedData fpsSavedData = FpsSavedData.getInstance(player.server);
            Level level = player.level();
            if (fpsSavedData.isEnableKAD()) {
                CommonProxy.getFPSCap(player).ifPresent(cap -> {
                    float currentDamage = event.getAmount();
                    Player assister = cap.checkAndGetAssister(level);
                    boolean isNewAssister = true;
                    //如果当前造成伤害玩家为助攻最高玩家
                    if (assister != null && assister.getUUID().equals(sourceP.getUUID())) {
                        cap.setAssisterDamage(cap.getAssisterDamage() + currentDamage);
                        isNewAssister = false;
                    }
                    //如果当前造成伤害玩家为助攻第二高玩家
                    assister = cap.checkAndGetAssister2(level);
                    if (assister != null && assister.getUUID().equals(sourceP.getUUID())) {
                        cap.setAssisterDamage2(cap.getAssisterDamage2() + currentDamage);
                        isNewAssister = false;
                    }
                    //如果都不是
                    if (isNewAssister) {
                        //若新助攻玩家伤害damage: damage > 最高助攻
                        if (currentDamage > cap.getAssisterDamage()) {
                            //第二变原第一
                            cap.setAssister2(cap.checkAndGetAssister(level));
                            cap.setAssisterDamage2(cap.getAssisterDamage());
                            //第一变新助攻玩家
                            cap.setAssisterDamage(currentDamage);
                            cap.setAssister(sourceP);
                            //若新助攻玩家伤害damage: 最高助攻 >= damage > 第二助攻
                        } else if (currentDamage > cap.getAssisterDamage2()) {
                            //第二变新助攻玩家
                            cap.setAssisterDamage2(currentDamage);
                            cap.setAssister2(sourceP);
                        }
                    }
                    //再次检查伤害大小, 进行排序
                    if (cap.getAssisterDamage2() > cap.getAssisterDamage()) {
                        //玩家互换
                        Player assister1 = cap.checkAndGetAssister(level);
                        cap.setAssister(cap.checkAndGetAssister2(level));
                        cap.setAssister2(assister1);
                        //伤害互换
                        float assisterDamage1 = cap.getAssisterDamage();
                        cap.setAssisterDamage(cap.getAssisterDamage2());
                        cap.setAssisterDamage2(assisterDamage1);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            FpsSavedData fpsSavedData = FpsSavedData.getInstance(killer.server);
            Level level = killer.level();
            if (fpsSavedData.isEnableKAD()) {
                ServerSynchedKADData killerKAD = fpsSavedData.getOrPutKAD(killer);
                //杀敌+1 (并非自己杀自己时)
                if (killer != event.getEntity()) {
                    killerKAD.modifyKAD(KAD.KAD_GENERAL, kad -> kad.kills(kad.kills + 1));
                    killerKAD.modifyKAD(KAD.KAD_CURRENT, kad -> kad.kills(kad.kills + 1));
                }
                //存在杀人玩家时
                if (event.getEntity() instanceof ServerPlayer killed) {
                    ServerSynchedKADData killedKAD = fpsSavedData.getOrPutKAD(killed);
                    //死亡+1
                    killedKAD.modifyKAD(KAD.KAD_GENERAL, kad -> kad.deaths(kad.deaths + 1));
                    killedKAD.modifyKAD(KAD.KAD_CURRENT, kad -> kad.deaths(kad.deaths + 1));
                    //获取真正的助攻者
                    MutableObject<Player> assisterMO = new MutableObject<>(null);
                    CommonProxy.getFPSCap(killed).ifPresent(cap -> {
                        Player assister = cap.checkAndGetAssister2(level);
                        //如果击杀玩家是造成伤害最多的助攻, 选择另一个玩家
                        //如果另一个玩家也是击杀玩家, 不进行操作
                        if (assister != null && !assister.getUUID().equals(killer.getUUID())) {
                            assisterMO.setValue(assister);
                        } else {
                            assister = cap.checkAndGetAssister(level);
                            if (assister != null && !assister.getUUID().equals(killer.getUUID()))
                                assisterMO.setValue(assister);
                        }
                        //清空助攻信息
                        cap.setAssister(null);
                        cap.setAssister2(null);
                    });
                    Player assister = assisterMO.getValue();
                    if (assister != null) {
                        ServerSynchedKADData assisterKAD = fpsSavedData.getOrPutKAD(assister);
                        assisterKAD.modifyKAD(KAD.KAD_GENERAL, kad -> kad.assists(kad.assists + 1));
                        assisterKAD.modifyKAD(KAD.KAD_CURRENT, kad -> kad.assists(kad.assists + 1));
                    }
                }
            }
        }
    }
}
