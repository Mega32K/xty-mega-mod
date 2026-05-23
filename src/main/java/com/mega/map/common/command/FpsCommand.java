package com.mega.map.common.command;

import com.mega.map.common.data.fps.FpsSavedData;
import com.mega.map.common.data.fps.kad.KAD;
import com.mega.map.common.data.fps.kad.SynchedKADData;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.s2c.map2.game2.S2CDeadPostEffectPacket;
import com.mega.map.proxy.CommonProxy;
import com.mega.map.common.warehouse.WeaponWarehouseItems;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class FpsCommand {
    private static final int DEFAULT_DEAD_POST_EFFECT_SECONDS = 3;

    private static final SuggestionProvider<CommandSourceStack> KAD_DEFAULT_KEY_PROVIDER = new SuggestionProvider<CommandSourceStack>() {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return builder.suggest(KAD.KAD_GENERAL).suggest(KAD.KAD_CURRENT).buildFuture();
        }
    };
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("fps")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.literal("kad")
                        .then(Commands.literal("start")
                                .executes(context -> start(context.getSource()))
                        )
                        .then(Commands.literal("stop")
                                .executes(context -> stop(context.getSource()))
                        )
                        .then(Commands.literal("clear")
                                .executes(context -> clear(context.getSource()))
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("key", StringArgumentType.word())
                                                .suggests(KAD_DEFAULT_KEY_PROVIDER)
                                                .executes(context -> clearPlayers(context.getSource(), EntityArgument.getPlayers(context, "players"), StringArgumentType.getString(context, "key")))
                                        )
                                )
                        )
                        .then(Commands.literal("modify")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("key", StringArgumentType.word())
                                                .suggests(KAD_DEFAULT_KEY_PROVIDER)
                                                .then(Commands.literal("kills")
                                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 255))
                                                                .executes(context -> setPlayerKills(context.getSource(), EntityArgument.getPlayers(context, "players"), StringArgumentType.getString(context, "key"), IntegerArgumentType.getInteger(context, "value")))
                                                        )
                                                )
                                                .then(Commands.literal("assists")
                                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 255))
                                                                .executes(context -> setPlayerAssists(context.getSource(), EntityArgument.getPlayers(context, "players"), StringArgumentType.getString(context, "key"), IntegerArgumentType.getInteger(context, "value")))
                                                        )
                                                )
                                                .then(Commands.literal("deaths")
                                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 255))
                                                                .executes(context -> setPlayerDeaths(context.getSource(), EntityArgument.getPlayers(context, "players"), StringArgumentType.getString(context, "key"), IntegerArgumentType.getInteger(context, "value")))
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("key", StringArgumentType.word())
                                                .suggests(KAD_DEFAULT_KEY_PROVIDER)
                                                .then(Commands.argument("kills", IntegerArgumentType.integer(0, 255))
                                                        .then(Commands.argument("assists", IntegerArgumentType.integer(0, 255))
                                                                .then(Commands.argument("deaths", IntegerArgumentType.integer(0, 255))
                                                                        .executes(context -> setPlayerKAD(
                                                                                context.getSource(),
                                                                                EntityArgument.getPlayers(context, "players"),
                                                                                StringArgumentType.getString(context, "key"),
                                                                                KAD.deserialize(0)
                                                                                        .kills(IntegerArgumentType.getInteger(context, "kills"))
                                                                                        .assists(IntegerArgumentType.getInteger(context, "assists"))
                                                                                        .deaths(IntegerArgumentType.getInteger(context, "deaths"))
                                                                        ))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("key", StringArgumentType.word())
                                                .suggests(KAD_DEFAULT_KEY_PROVIDER)
                                                .then(Commands.literal("kills")
                                                        .executes(context -> getPlayerKills(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "key")))
                                                )
                                                .then(Commands.literal("assists")
                                                        .executes(context -> getPlayerAssists(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "key")))
                                                )
                                                .then(Commands.literal("deaths")
                                                        .executes(context -> getPlayerDeaths(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "key")))
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("effect")
                        .then(Commands.literal("aspect43")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> setAspect43Effect(context.getSource(), EntityArgument.getPlayers(context, "targets"), true))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(context -> setAspect43Effect(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        BoolArgumentType.getBool(context, "enabled")
                                                ))
                                        )
                                )
                        )
                        .then(Commands.literal("dead")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> startDeadPostEffect(context.getSource(), EntityArgument.getPlayers(context, "targets"), DEFAULT_DEAD_POST_EFFECT_SECONDS))
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                                                .executes(context -> startDeadPostEffect(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "seconds")))
                                        )
                                )
                        )
                )
                .then(Commands.literal("warehouseBlacklist")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> addWarehouseGunBlacklist(context.getSource(), EntityArgument.getPlayer(context, "player")))
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> removeWarehouseGunBlacklist(context.getSource(), EntityArgument.getPlayer(context, "player")))
                                )
                        )
                        .then(Commands.literal("clear")
                                .executes(context -> clearWarehouseGunBlacklist(context.getSource()))
                        )
                        .then(Commands.literal("get")
                                .executes(context -> getWarehouseGunBlacklist(context.getSource()))
                        )
                )
                .then(Commands.literal("bomb")
                        .then(Commands.literal("set")
                                .then(Commands.literal("exist")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBombExist(context.getSource(), BoolArgumentType.getBool(context, "value")))
                                        )
                                )
                                .then(Commands.literal("position")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 2))
                                                .executes(context -> setBombPosition(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                        )
                                )
                                .then(Commands.literal("countdown")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(context -> setBombCountdown(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                        )
                                )
                        )
                        .then(Commands.literal("get")
                                .then(Commands.literal("exist")
                                        .executes(context -> getBombExist(context.getSource()))
                                )
                                .then(Commands.literal("position")
                                        .executes(context -> getBombPosition(context.getSource()))
                                )
                                .then(Commands.literal("countdown")
                                        .executes(context -> getBombCountdown(context.getSource()))
                                )
                        )
                );
    }
    private static int start(CommandSourceStack sourceStack) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        savedData.setEnableKAD(true);
        return 0;
    }

    private static int stop(CommandSourceStack sourceStack) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        savedData.setEnableKAD(false);
        return 0;
    }
    private static int clear(CommandSourceStack sourceStack) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        savedData.clearKAD();
        return 0;
    }
    private static int clearPlayers(CommandSourceStack sourceStack, Collection<? extends ServerPlayer> players, String key) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        for (ServerPlayer player : players) {
            savedData.getOrPutKAD(player).setKAD(key, KAD.deserialize(0));
        }
        return players.size();
    }
    private static int setPlayerKAD(CommandSourceStack sourceStack, Collection<? extends ServerPlayer> players, String key, KAD kad) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        for (ServerPlayer player : players) {
            savedData.getOrPutKAD(player).setKAD(key, kad);
        }
        return players.size();
    }
    private static int setPlayerKills(CommandSourceStack sourceStack, Collection<? extends ServerPlayer> players, String key, int kills) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        for (ServerPlayer player : players) {
            savedData.getOrPutKAD(player).modifyKAD(key, kad -> kad.kills(kills));
        }
        return players.size();
    }
    private static int setPlayerAssists(CommandSourceStack sourceStack, Collection<? extends ServerPlayer> players, String key, int assists) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        for (ServerPlayer player : players) {
            savedData.getOrPutKAD(player).modifyKAD(key, kad -> kad.assists(assists));
        }
        return players.size();
    }
    private static int setPlayerDeaths(CommandSourceStack sourceStack, Collection<? extends ServerPlayer> players, String key, int deaths) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        for (ServerPlayer player : players) {
            savedData.getOrPutKAD(player).modifyKAD(key, kad -> kad.deaths(deaths));
        }
        return players.size();
    }
    private static int getPlayerKills(CommandSourceStack sourceStack, ServerPlayer serverPlayer, String key) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        return savedData.getKadData().getOrDefault(serverPlayer.getUUID(), SynchedKADData.EMPTY_KAD).getOrDefaultKAD(key).kills;
    }
    private static int getPlayerAssists(CommandSourceStack sourceStack, ServerPlayer serverPlayer, String key) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        return savedData.getKadData().getOrDefault(serverPlayer.getUUID(), SynchedKADData.EMPTY_KAD).getOrDefaultKAD(key).assists;
    }
    private static int getPlayerDeaths(CommandSourceStack sourceStack, ServerPlayer serverPlayer, String key) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        return savedData.getKadData().getOrDefault(serverPlayer.getUUID(), SynchedKADData.EMPTY_KAD).getOrDefaultKAD(key).deaths;
    }
    private static int setBombExist(CommandSourceStack sourceStack, boolean value) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        savedData.setBombExist(value);
        savedData.setBombCountdownTicks(savedData.getBombCountdownTicks());
        sourceStack.sendSuccess(() -> Component.literal("bombExist = " + value), false);
        return value ? 1 : 0;
    }
    private static int setBombPosition(CommandSourceStack sourceStack, int value) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        savedData.setBombPosition((byte) value);
        savedData.setBombCountdownTicks(savedData.getBombCountdownTicks());
        sourceStack.sendSuccess(() -> Component.literal("bombPosition = " + value), false);
        return value;
    }
    private static int setBombCountdown(CommandSourceStack sourceStack, int value) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        savedData.setBombCountdownTicks(value * 20);
        sourceStack.sendSuccess(() -> Component.literal("bombCountdownSeconds = " + value), false);
        return value;
    }
    private static int getBombExist(CommandSourceStack sourceStack) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.bomb.exist.get", savedData.isBombExist()), false);
        return savedData.isBombExist() ? 1 : 0;
    }
    private static int getBombPosition(CommandSourceStack sourceStack) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.bomb.position.get", savedData.getBombPosition()), false);
        return savedData.getBombPosition();
    }
    private static int getBombCountdown(CommandSourceStack sourceStack) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.bomb.countdown.get", savedData.getBombCountdownTicks()), false);
        return savedData.getBombCountdownTicks();
    }

    private static int startDeadPostEffect(CommandSourceStack sourceStack, Collection<? extends ServerPlayer> players, int seconds) {
        int durationTicks = seconds * 20;
        for (ServerPlayer player : players) {
            NetworkHandler.sendToPlayer(new S2CDeadPostEffectPacket(durationTicks), player);
        }
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.effect.dead.start", players.size(), seconds), false);
        return players.size();
    }

    private static int setAspect43Effect(CommandSourceStack sourceStack, Collection<? extends ServerPlayer> players, boolean enabled) {
        for (ServerPlayer player : players) {
            CommonProxy.getFPSCap(player).ifPresent(cap -> cap.setAspect43(enabled));
        }
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.effect.aspect43.set", players.size(), enabled), false);
        return players.size();
    }

    private static int addWarehouseGunBlacklist(CommandSourceStack sourceStack, ServerPlayer player) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        ResourceLocation gunId = WeaponWarehouseItems.getGunIdFromMainHand(player);
        if (gunId == null) {
            sourceStack.sendFailure(Component.translatable("commands.megamod.message.fps.warehouse_blacklist.invalid"));
            return 0;
        }
        boolean added = savedData.addWarehouseGunBlacklist(gunId);
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.warehouse_blacklist.add", gunId.toString(), added), false);
        return added ? 1 : 0;
    }

    private static int removeWarehouseGunBlacklist(CommandSourceStack sourceStack, ServerPlayer player) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        ResourceLocation gunId = WeaponWarehouseItems.getGunIdFromMainHand(player);
        if (gunId == null) {
            sourceStack.sendFailure(Component.translatable("commands.megamod.message.fps.warehouse_blacklist.invalid"));
            return 0;
        }
        boolean removed = savedData.removeWarehouseGunBlacklist(gunId);
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.warehouse_blacklist.remove", gunId.toString(), removed), false);
        return removed ? 1 : 0;
    }

    private static int clearWarehouseGunBlacklist(CommandSourceStack sourceStack) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        savedData.clearWarehouseGunBlacklist();
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.warehouse_blacklist.clear"), false);
        return 0;
    }

    private static int getWarehouseGunBlacklist(CommandSourceStack sourceStack) {
        FpsSavedData savedData = FpsSavedData.getInstance(sourceStack.getServer());
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.fps.warehouse_blacklist.get", savedData.getWarehouseGunBlacklist().size()), false);
        return savedData.getWarehouseGunBlacklist().size();
    }
}
