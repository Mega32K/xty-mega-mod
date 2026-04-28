package com.mega.xty.common.event;

import com.mega.xty.common.capability.FpsCapability;
import com.mega.xty.common.capability.Map2Capability;
import com.mega.xty.common.data.fps.DeathSourceType;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.map2.Game1SavedData;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.data.map2.ServerGameData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.game2.S2CGame2DeathEffectPacket;
import com.mega.xty.proxy.CommonProxy;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.scores.Team;
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
    private static final double GAME2_DEATH_CAMERA_DISTANCE = 3.0D;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            if (event.getSource().getEntity() instanceof ServerPlayer killer && entity instanceof ServerPlayer deathP) {
                if (ServerGameData.map2Playing(server)) {
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
                        Map2SavedData map2SavedData = Map2SavedData.getInstance(server);
                        if (map2SavedData.isTeamMode()) {
                            Vec3 deathPos = deathP.position();
                            Vec3 deathCameraStart = deathP.getEyePosition();
                            float deathCameraXRot = deathP.getXRot();
                            float deathCameraYRot = deathP.getYRot();
                            ResourceKey<Level> dimension = deathP.level().dimension();
                            deathP.respawn();
                            ServerLevel dimensionLevel = server.getLevel(dimension);
                            if (dimensionLevel != null)
                                deathP.teleportTo(dimensionLevel, deathPos.x, deathPos.y, deathPos.z, 0, 0);
                            CommonProxy.getMap2Cap(deathP).ifPresent(cap -> cap.setXaeroDead(true));
                            if (!Game2SavedData.getInstance(server).isStopped()) {
                                playGame2DeathEffect(killer, deathP, deathCameraStart, deathCameraXRot, deathCameraYRot);
                                finishGame2RoundIfTeamAllDead(server, deathP);
                                Game2SavedData savedData = Game2SavedData.getInstance(server);
                                String func = savedData.getGame2Functions().getOnPlayerDeathFunction();
                                if (func != null && !func.isEmpty())
                                    server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f -> server.getFunctions().execute(f, deathP.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2)));
                            } else {
                                Game1SavedData savedData = Game1SavedData.getInstance(server);
                                String func = savedData.getGame1Functions().getOnPlayerDeathFunction();
                                if (func != null && !func.isEmpty())
                                    server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f -> server.getFunctions().execute(f, deathP.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2)));
                            }
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
                    if (assister != null && !assister.getUUID().equals(killer.getUUID()))
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

    private static void playGame2DeathEffect(ServerPlayer killer, ServerPlayer deadPlayer, Vec3 deathCameraStart, float xRot, float yRot) {
        Vec3 killerPos = killer.getEyePosition();
        Vec3 direction = deathCameraStart.subtract(killerPos);
        if (direction.lengthSqr() < 1.0E-7D) {
            direction = deadPlayer.getLookAngle();
        }
        if (direction.lengthSqr() < 1.0E-7D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }

        Vec3 deathCameraEnd = deathCameraStart.add(direction.normalize().scale(GAME2_DEATH_CAMERA_DISTANCE));
        NetworkHandler.sendToPlayer(new S2CGame2DeathEffectPacket(
                deathCameraStart,
                deathCameraEnd,
                xRot,
                yRot
        ), deadPlayer);
    }

    private static void finishGame2RoundIfTeamAllDead(MinecraftServer server, ServerPlayer deadPlayer) {
        Team team = deadPlayer.getTeam();
        if (team == null) {
            return;
        }
        ChatFormatting color = team.getColor();
        if (color != ChatFormatting.RED && color != ChatFormatting.BLUE) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Team playerTeam = player.getTeam();
            if (playerTeam == null || playerTeam.getColor() != color) {
                continue;
            }
            boolean dead = CommonProxy.getMap2Cap(player).map(Map2Capability::isXaeroDead).orElse(false);
            if (!dead) {
                return;
            }
        }
        if (color == ChatFormatting.RED && FpsSavedData.getInstance(server).isBombExist()) {
            return;
        }
        Map2SavedData.getInstance(server).finish(color == ChatFormatting.BLUE);
    }
}
