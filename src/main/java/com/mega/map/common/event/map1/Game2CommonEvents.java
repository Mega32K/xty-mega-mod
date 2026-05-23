package com.mega.map.common.event.map1;

import com.mega.endinglib.mixin.accessor.AccessorItemEntity;
import com.mega.map.common.data.map1.Game2SavedData;
import com.mega.map.common.entity.Game2ItemEntity;
import com.mega.map.common.entity.ThrownItemEntity;
import com.mega.map.common.init.EntityInit;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.s2c.S2CDisableBIPacket;
import com.mega.map.common.network.s2c.map1.game2.S2CGame2HitEffectPacket;
import com.mega.map.common.network.s2c.map1.game2.S2CGame2StatsPacket;
import com.mega.map.common.tags.XtyDamageTypeTags;
import com.mega.map.proxy.CommonProxy;
import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Game2CommonEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            Game2SavedData data = Game2SavedData.getInstance(server);
            NetworkHandler.sendToPlayer(new S2CGame2StatsPacket(data.isStopped(), data.isSceneChanging()), serverPlayer);
            NetworkHandler.sendToPlayer(new S2CDisableBIPacket(data.isDisableBlockInteraction()), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void game2Invulnerable(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getSource().is(XtyDamageTypeTags.GAME_INVULNERABLE_BYPASS2))
                CommonProxy.getXtyCap(player).ifPresent(capability -> {
                    if (capability.isGameInvul2()) {
                        event.setCanceled(true);
                    }
                });
        }
    }
    @SubscribeEvent
    public static void game2DamageTransform(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.server;
            Game2SavedData savedData = Game2SavedData.getInstance(server);
            if (savedData.isStopped()) return;
            Scoreboard scoreboard = server.getScoreboard();
            String pName = player.getScoreboardName();
            Objective healthObj = savedData.getHealthObjective();
            boolean dead = false;
            if (healthObj != null) {
                {
                    Entity source = event.getSource().getEntity();
                    if (source instanceof Player sp) {
                        player.setLastHurtByMob(sp);
                        player.setLastHurtByPlayer(sp);
                        if (sp.getMainHandItem().isEmpty() || (!(sp.getMainHandItem().getItem() instanceof IGun) && !(sp.getMainHandItem().getItem() instanceof CrossbowItem)))
                            NetworkHandler.sendToAll(new S2CGame2HitEffectPacket(source.getId(), player.getId()));
                    }
                }
                Score healthScore = scoreboard.getOrCreatePlayerScore(pName, healthObj);
                int lastHealth = healthScore.getScore();
                healthScore.add((int) (-event.getAmount()));
                String onPlayerDeathFunc = savedData.getGame2Functions().getOnPlayerDeathFunction();
                if (healthScore.getScore() <= 0 && lastHealth > 0 && onPlayerDeathFunc != null && !onPlayerDeathFunc.isEmpty()) {
                    healthScore.setScore(-1);
                    dead = true;
                }

                String hurtByEntityFunction = savedData.getGame2Functions().getHurtByEntityFunction();
                if (hurtByEntityFunction != null && !hurtByEntityFunction.isEmpty()) {
                    if (event.getEntity() != event.getSource().getEntity() && event.getSource().getEntity() != null)
                        server.getFunctions().get(ResourceLocation.parse(hurtByEntityFunction)).ifPresent(commandFunction -> server.getFunctions().execute(commandFunction, player.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2)));
                }
                event.setAmount(0);
            }
            if (dead) {
                String onPlayerDeathFunc = savedData.getGame2Functions().getOnPlayerDeathFunction();
                if (onPlayerDeathFunc != null) {
                    server.getFunctions().get(ResourceLocation.parse(onPlayerDeathFunc)).ifPresent(commandFunction -> server.getFunctions().execute(commandFunction, player.createCommandSourceStack().withSuppressedOutput().withMaximumPermission(2)));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDropItem(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            Game2SavedData savedData = Game2SavedData.getInstance(serverPlayer.server);
            if (!savedData.isStopped()) {
                ItemEntity itemE = event.getEntity();
                com.mega.endinglib.proxy.CommonProxy.getEntityCapOptional(itemE).ifPresent(cap -> {
                    cap.setPickable(Optional.of(true));
                    cap.setCanBeCollideWith(Optional.of(true));
                    itemE.setDeltaMovement(itemE.getDeltaMovement().scale(4.0));
                    Game2ItemEntity game2ItemEntity = new Game2ItemEntity(itemE.level(), itemE.getX(), itemE.getY(), itemE.getZ(), itemE.getItem(), itemE.getDeltaMovement().x, itemE.getDeltaMovement().y, itemE.getDeltaMovement().z);
                    game2ItemEntity.setThrower(serverPlayer.getUUID());
                    game2ItemEntity.setTarget(((AccessorItemEntity) itemE).getTarget());
                    game2ItemEntity.setPickUpDelay(30);
                    com.mega.endinglib.proxy.CommonProxy.getEntityCapOptional(game2ItemEntity).ifPresent(c -> {
                        c.setCanBeCollideWith(Optional.of(true));
                    });
                    serverPlayer.level().addFreshEntity(game2ItemEntity);

                    ThrownItemEntity thrownItemEntity = new ThrownItemEntity(EntityInit.THROWN_ITEM.get(), serverPlayer.level());
                    thrownItemEntity.setOwner(serverPlayer);
                    thrownItemEntity.setBindingItem(game2ItemEntity);
                    serverPlayer.level().addFreshEntity(thrownItemEntity);
                    serverPlayer.level().levelEvent(110120, serverPlayer.blockPosition(), 2);
                    event.setCanceled(true);
                });
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            Game2SavedData savedData = Game2SavedData.getInstance(server);
            if (!savedData.isStopped()) {
                if (event.phase == TickEvent.Phase.START) {
                    CommonProxy.getXtyCap(serverPlayer).ifPresent(capability -> {
                        Scoreboard scoreboard = server.getScoreboard();
                        String pName = serverPlayer.getScoreboardName();
                        Objective healthObj = savedData.getHealthObjective();
                        if (healthObj != null)
                            capability.setGame2Health(scoreboard.getOrCreatePlayerScore(pName, healthObj).getScore());
                        Objective maxHealthObj = savedData.getMaxHealthObjective();
                        if (maxHealthObj != null)
                            capability.setGame2MaxHealth(scoreboard.getOrCreatePlayerScore(pName, maxHealthObj).getScore());
                    });
                }
            }
        }
    }
}
