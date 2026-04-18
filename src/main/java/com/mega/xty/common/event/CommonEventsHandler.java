package com.mega.xty.common.event;

import com.mega.xty.common.capability.FpsCapability;
import com.mega.xty.common.data.fps.DeathSourceType;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.data.map2.ServerGameData;
import com.mega.xty.proxy.CommonProxy;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;

@Mod.EventBusSubscriber
public class CommonEventsHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (event.getSource().getEntity() instanceof ServerPlayer killer && entity instanceof ServerPlayer deathP) {
                if (ServerGameData.map2Playing(serverLevel.getServer())) {
                    if (!entity.isAlive()) {
                        ItemStack itemStack = killer.getMainHandItem();
                        if (itemStack.isEmpty()) itemStack = killer.getOffhandItem();
                        final ItemStack weapon = itemStack;
                        CommonProxy.getFPSCap(killer).ifPresent(cap -> {
                            FpsCapability.DeathMessage message = cap.getOrDefaultDeathMessage(entity);
                            message.putIfAbsentKiller(makeKillerMessage(killer, entity));
                            message.setKilledWeapon(weapon);
                            message.putIfAbsentKilled(entity.getDisplayName());
                            message.makeDeathType(deathTypeMessage(event.getSource(), weapon, null));
                        });
                        Map2SavedData map2SavedData = Map2SavedData.getInstance(serverLevel.getServer());
                        if (map2SavedData.isTeamMode()) {
                            Vec3 deathPos = deathP.position();
                            ResourceKey<Level> dimension = deathP.level().dimension();
                            deathP.respawn();
                            ServerLevel dimensionLevel = serverLevel.getServer().getLevel(dimension);
                            if (dimensionLevel != null)
                                deathP.teleportTo(dimensionLevel, deathPos.x, deathPos.y, deathPos.z, 0, 0);
                            CommonProxy.getMap2Cap(deathP).ifPresent(cap -> cap.setXaeroDead(true));
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKilledByGun(EntityKillByGunEvent event) {
        LivingEntity entity = event.getKilledEntity();
        if (entity == null) return;
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (event.getAttacker() instanceof ServerPlayer killer /*&& entity instanceof ServerPlayer deathP*/) {
                if (ServerGameData.map2Playing(serverLevel.getServer())) {
                    if (!entity.isAlive()) {
                        final ItemStack weapon = killer.getMainHandItem();
                        CommonProxy.getFPSCap(killer).ifPresent(cap -> {
                            FpsCapability.DeathMessage message = cap.getOrDefaultDeathMessage(entity);
                            message.putIfAbsentKiller(makeKillerMessage(killer, entity));
                            message.setKilledWeapon(weapon);
                            message.putIfAbsentKilled(entity.getDisplayName());
                            message.makeDeathType(deathTypeMessage(event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING), weapon, event));
                        });
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp)
            CommonProxy.getMap2Cap(sp).ifPresent(cap -> {
                int v = !sp.isShiftKeyDown() && !sp.hasPose(Pose.SWIMMING) ? -3 : 3;
                cap.setSoulInvisible(cap.getSoulInvisible() + v);
                cap.setSoulInvisible(Mth.clamp(cap.getSoulInvisible(), 0, 15));
            });
    }
    public static MutableComponent makeKillerMessage(Player killer, LivingEntity death) {
        MutableComponent message = Component.literal("").append(killer.getDisplayName());
        //添加助攻信息
        if (death instanceof Player deathP)
            CommonProxy.getFPSCap(deathP).ifPresent(cap -> {
                if (cap.getAssisterDamage() >= 41F) {
                    Player assister = cap.checkAndGetAssister(deathP.level());
                    if (assister != null)
                        message.append(" + ").append(assister.getDisplayName());
                }
            });
        return message;
    }
    public static Collection<DeathSourceType> deathTypeMessage(DamageSource damageSource, ItemStack weapon, @Nullable EntityKillByGunEvent killByGunEvent) {
        EnumSet<DeathSourceType> set = EnumSet.noneOf(DeathSourceType.class);
        if (killByGunEvent == null) {
            if (!set.contains(DeathSourceType.HEADSHOT) && !set.contains(DeathSourceType.DEFAULT))
                set.add(DeathSourceType.SLASH);
            if (damageSource.is(DamageTypeTags.IS_FIRE)) {
                set.add(DeathSourceType.BURN);
            } else if (damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
                set.add(DeathSourceType.BLAST);
            }
        } else {
            set.remove(DeathSourceType.SLASH);
            if (killByGunEvent.isHeadShot()) {
                set.add(DeathSourceType.HEADSHOT);
                set.remove(DeathSourceType.DEFAULT);
            } else {
                set.add(DeathSourceType.DEFAULT);
                set.remove(DeathSourceType.HEADSHOT);
            }
        }

        return set;
    }
}
