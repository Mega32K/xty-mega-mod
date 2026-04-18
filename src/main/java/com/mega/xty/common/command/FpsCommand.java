package com.mega.xty.common.command;

import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.data.fps.kad.SynchedKADData;
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
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FpsCommand {
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
}
