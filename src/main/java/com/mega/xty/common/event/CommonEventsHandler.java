package com.mega.xty.common.event;

import com.mega.endinglib.api.item.component.ItemComponentManager;
import com.mega.xty.common.capability.FpsCapability;
import com.mega.xty.common.capability.Map2Capability;
import com.mega.xty.common.component.ComponentInit;
import com.mega.xty.common.component.ItemSwitchComponent;
import com.mega.xty.common.data.fps.DeathSourceType;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.data.fps.kad.ServerSynchedKADData;
import com.mega.xty.common.data.map2.Game1SavedData;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.data.map2.ServerGameData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.S2CAddDeathDataPacket;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class CommonEventsHandler {
    private static final double GAME2_DEATH_CAMERA_DISTANCE = 3.0D;
    private static final Map<UUID, HeldItemSnapshot> LAST_HELD_ITEMS = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            if (entity instanceof ServerPlayer deathP && ServerGameData.map2Playing(server) && !entity.isAlive()) {
                if (event.getSource().getEntity() instanceof ServerPlayer killer) {
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
                    handleMap2TeamPlayerDeath(server, deathP, killer);
                } else if (event.getSource().is(DamageTypes.FALL) && isGame2Playing(server)) {
                    handleGame2FallDeath(server, deathP);
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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        HeldItemSnapshot previous = LAST_HELD_ITEMS.get(player.getUUID());
        if (previous != null) {
            checkItemSwitch(player, InteractionHand.MAIN_HAND, mainHand, previous);
            checkItemSwitch(player, InteractionHand.OFF_HAND, offHand, previous);
        }
        LAST_HELD_ITEMS.put(player.getUUID(), new HeldItemSnapshot(mainHand.copy(), offHand.copy()));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_HELD_ITEMS.remove(event.getEntity().getUUID());
    }

    private static void checkItemSwitch(ServerPlayer player, InteractionHand hand, ItemStack current, HeldItemSnapshot previous) {
        if (current.isEmpty() || previous.contains(current)) {
            return;
        }
        ItemSwitchComponent component = ItemComponentManager.get(current, ComponentInit.ITEM_SWITCH);
        if (component != null) {
            component.apply(player, hand);
        }
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

    private static void handleGame2FallDeath(MinecraftServer server, ServerPlayer deathP) {
        addFallDeathKAD(server, deathP);
        CommonProxy.getFPSCap(deathP).ifPresent(cap -> {
            cap.deathMessageMap.clear();
            cap.setAssister(null);
            cap.setAssister2(null);
            cap.setAssisterDamage(0.0F);
            cap.setAssisterDamage2(0.0F);
        });
        NetworkHandler.sendToAll(new S2CAddDeathDataPacket(
                Component.literal("").append(deathP.getDisplayName()).append(" 摔死了"),
                ItemStack.EMPTY,
                Component.empty()
        ));
        handleMap2TeamPlayerDeath(server, deathP, null);
    }

    private static void addFallDeathKAD(MinecraftServer server, ServerPlayer deathP) {
        FpsSavedData fpsSavedData = FpsSavedData.getInstance(server);
        if (!fpsSavedData.isEnableKAD()) {
            return;
        }
        ServerSynchedKADData killedKAD = fpsSavedData.getOrPutKAD(deathP);
        killedKAD.modifyKAD(KAD.KAD_GENERAL, kad -> kad.deaths(kad.deaths + 1));
        killedKAD.modifyKAD(KAD.KAD_CURRENT, kad -> kad.deaths(kad.deaths + 1));
    }

    private static boolean isGame2Playing(MinecraftServer server) {
        return ServerGameData.map2Playing(server)
                && Map2SavedData.getInstance(server).isTeamMode()
                && !Game2SavedData.getInstance(server).isStopped();
    }

    private static void handleMap2TeamPlayerDeath(MinecraftServer server, ServerPlayer deathP, @Nullable ServerPlayer killer) {
        Map2SavedData map2SavedData = Map2SavedData.getInstance(server);
        if (!map2SavedData.isTeamMode()) {
            return;
        }
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

    private static void playGame2DeathEffect(@Nullable ServerPlayer killer, ServerPlayer deadPlayer, Vec3 deathCameraStart, float xRot, float yRot) {
        Vec3 direction = killer != null ? deathCameraStart.subtract(killer.getEyePosition()) : deadPlayer.getLookAngle().scale(-1.0D);
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

    private record HeldItemSnapshot(ItemStack mainHand, ItemStack offHand) {
        private boolean contains(ItemStack stack) {
            return sameItemStack(this.mainHand, stack) || sameItemStack(this.offHand, stack);
        }

        private static boolean sameItemStack(ItemStack first, ItemStack second) {
            return !first.isEmpty() && !second.isEmpty() && ItemStack.isSameItemSameTags(first, second);
        }
    }
}
