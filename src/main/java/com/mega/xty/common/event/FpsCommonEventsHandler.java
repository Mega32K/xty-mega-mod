package com.mega.xty.common.event;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.data.fps.kad.ServerSynchedKADData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.fps.S2CPlayerKADPacket;
import com.mega.xty.common.network.s2c.fps.S2CPlayerNamePacket;
import com.mega.xty.common.network.s2c.fps.S2CUsingKADPacket;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableObject;

@Mod.EventBusSubscriber(modid = XtyMegaMod.MODID)
public class FpsCommonEventsHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            FpsSavedData data = FpsSavedData.getInstance(server);
            NetworkHandler.sendToPlayer(new S2CUsingKADPacket(data.isEnableKAD()), serverPlayer);
            if (data.isEnableKAD()) {
                NetworkHandler.sendToPlayer(new S2CPlayerKADPacket(true, data.getKadData()), serverPlayer);
            }
            NetworkHandler.sendToPlayer(new S2CPlayerNamePacket(true, data.getPlayerTabData()), serverPlayer);
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
                    if (assister != null && assister.getUUID().equals(sourceP.getUUID())) {
                        cap.setAssisterDamage(cap.getAssisterDamage() + currentDamage);
                    } else {
                        assister = cap.checkAndGetAssister2(level);
                        if (assister != null && assister.getUUID().equals(sourceP.getUUID())) {
                            cap.setAssisterDamage2(cap.getAssisterDamage2() + currentDamage);
                        } else {
                            if (currentDamage > cap.getAssisterDamage()) {
                                cap.setAssister2(cap.checkAndGetAssister2(level));
                                cap.setAssisterDamage2(cap.getAssisterDamage());
                                cap.setAssisterDamage(currentDamage);
                                cap.setAssister(sourceP);
                            } else if (currentDamage > cap.getAssisterDamage2()) {
                                cap.setAssisterDamage2(currentDamage);
                                cap.setAssister2(sourceP);
                            }
                        }
                    }
                    //再次检查伤害大小
                    if (cap.getAssisterDamage2() > cap.getAssisterDamage()) {
                        Player assister1 = cap.checkAndGetAssister(level);
                        cap.setAssister(cap.checkAndGetAssister2(level));
                        cap.setAssister2(assister1);
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
                        Player assister = cap.checkAndGetAssister(level);
                        //如果击杀玩家是造成伤害最多的助攻, 选择另一个玩家
                        if (assister != null && assister.getUUID().equals(killer.getUUID())) {
                            assisterMO.setValue(cap.checkAndGetAssister2(level));
                        } else {
                            assisterMO.setValue(cap.checkAndGetAssister(level));
                        }
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
