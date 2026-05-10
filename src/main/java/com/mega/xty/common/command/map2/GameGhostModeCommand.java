package com.mega.xty.common.command.map2;

import com.mega.xty.common.data.map2.*;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GameGhostModeCommand {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (p_137719_, p_137720_) -> {
        ServerFunctionManager serverfunctionmanager = p_137719_.getSource().getServer().getFunctions();
        return SharedSuggestionProvider.suggestResource(serverfunctionmanager.getFunctionNames(), p_137720_);
    };
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("map2game2")
                .requires(stack -> stack.hasPermission(1))
                .then(Commands.literal("start")
                        .executes(context -> start(context.getSource()))
                )
                .then(Commands.literal("stop")
                        .executes(context -> stop(context.getSource()))
                )
                .then(Commands.literal("isStopped")
                        .executes(context -> Game2SavedData.getInstance(context.getSource().getServer()).isStopped() ? 0 : 1)
                ) 
                .then(Commands.literal("game2Dimension")
                        .then(Commands.literal("set")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(context -> setGame2Dimension(context.getSource(), DimensionArgument.getDimension(context, "dimension")))
                                )
                        )
                        .then(Commands.literal("get")
                                .executes(context -> getGame2Dimension(context.getSource()))
                        )
                        .then(Commands.literal("clear")
                                .executes(context -> clearGame2Dimension(context.getSource()))
                        )
                )
                .then(Commands.literal("startNewRound")
                        .executes(context -> startGame2NewRound(context.getSource()))
                )
                .then(Map2Game2OptionsCommand.register())
                .then(Game2WarehouseMeleeCommand.register())
                .then(Commands.literal("home")
                        .then(Commands.literal("red")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> addRedHome(context.getSource(), BlockPosArgument.getBlockPos(context, "pos")))
                                        )
                                )
                                .then(Commands.literal("clear")
                                        .executes(context -> clearRedHome(context.getSource()))
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getRedHome(context.getSource()))
                                )
                        )
                        .then(Commands.literal("blue")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> addBlueHome(context.getSource(), BlockPosArgument.getBlockPos(context, "pos")))
                                        )
                                )
                                .then(Commands.literal("clear")
                                        .executes(context -> clearBlueHome(context.getSource()))
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getBlueHome(context.getSource()))
                                )
                        )
                        .then(Commands.literal("back")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> backToHome(context.getSource(), EntityArgument.getPlayers(context, "players")))
                                )
                        )
                )
                .then(Commands.literal("function")
                        .then(Commands.literal("onPlayerDeath")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game2Functions::setOnPlayerDeathFunction, "on_player_death.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game2Functions::getOnPlayerDeathFunction, "on_player_death.get"))
                                )
                        )
                        .then(Commands.literal("start")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game2Functions::setStartFunction, "start.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game2Functions::getStartFunction, "start.get"))
                                )
                        )
                        .then(Commands.literal("startNewRound")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game2Functions::setStartNewRoundFunction, "start_new_round.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game2Functions::getStartNewRoundFunction, "start_new_round.get"))
                                )
                        )
                        .then(Commands.literal("stop")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game2Functions::setStopFunction, "stop.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game2Functions::getStopFunction, "stop.get"))
                                )
                        )
                );
    }
    private static int start(CommandSourceStack sourceStack) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        data.setStopped(false);
        Entity entity = sourceStack.getEntity();
        if (entity != null) {
            CommandSourceStack sourceStack2 = sourceStack.withSuppressedOutput().withMaximumPermission(2);
            MinecraftServer server = sourceStack2.getServer();
            String func = data.getGame2Functions().getStartFunction();
            if (func != null && !func.isEmpty()) {
                server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f -> server.getFunctions().execute(f, sourceStack2));
            }
        }
        return 1;
    }
    private static int stop(CommandSourceStack sourceStack) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        data.setStopped(true);
        Entity entity = sourceStack.getEntity();
        if (entity != null) {
            CommandSourceStack sourceStack2 = sourceStack.withSuppressedOutput().withMaximumPermission(2);
            MinecraftServer server = sourceStack2.getServer();
            String func = data.getGame2Functions().getStopFunction();
            if (func != null && !func.isEmpty()) {
                server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f-> server.getFunctions().execute(f, sourceStack2));
            }
        }
        return 1;
    }
    private static int setGame2Dimension(CommandSourceStack sourceStack, ServerLevel level) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setGame2Dimension(level.dimension());
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.game2_dimension.set", level.dimension().location().toString()), false);
        return 0;
    }
    private static int getGame2Dimension(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        String value = data.getGame2Dimension() == null ? "null" : data.getGame2Dimension().location().toString();
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.game2_dimension.get", value), false);
        return 0;
    }
    private static int clearGame2Dimension(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setGame2Dimension(null);
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.game2_dimension.clear"), false);
        return 0;
    }
    private static int startGame2NewRound(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.startGame2NewRound();
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.start_new_round"), false);
        return 1;
    }
    private static int addRedHome(CommandSourceStack sourceStack, BlockPos pos) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        List<BlockPos> homes = new ArrayList<>(data.getRedHome());
        if (homes.size() < 10) {
            homes.add(pos);
            sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.home.red.add", pos.toShortString()), false);
        }
        data.setRedHome(homes);
        return homes.size();
    }
    private static int clearRedHome(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setRedHome(List.of());
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.home.red.clear"), false);
        return 0;
    }
    private static int getRedHome(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        String value = formatHomes(data.getRedHome());
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.home.red.get", value), false);
        return data.getRedHome().size();
    }
    private static int addBlueHome(CommandSourceStack sourceStack, BlockPos pos) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        List<BlockPos> homes = new ArrayList<>(data.getBlueHome());
        if (homes.size() < 10) {
            homes.add(pos);
            sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.home.blue.add", pos.toShortString()), false);
        }
        data.setBlueHome(homes);
        return homes.size();
    }
    private static int clearBlueHome(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setBlueHome(List.of());
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.home.blue.clear"), false);
        return 0;
    }
    private static int getBlueHome(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        String value = formatHomes(data.getBlueHome());
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.home.blue.get", value), false);
        return data.getBlueHome().size();
    }
    private static int backToHome(CommandSourceStack sourceStack, Collection<ServerPlayer> players) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.backToHome(new ArrayList<>(players));
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.map2.home.back", players.size()), false);
        return players.size();
    }
    private static int setFunction(CommandSourceStack sourceStack, Collection<CommandFunction> functions, BiConsumer<Game2Functions, String> setter, String lang) {
        lang = "commands.xtymegamod.message.map2.game2.function."+lang;
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        if (functions.isEmpty())
            return 0;
        String function = functions.iterator().next().getId().toString();
        setter.accept(data.getGame2Functions(),function);
        String finalLang = lang;
        sourceStack.sendSuccess(()-> Component.translatable(finalLang, function), false);
        return functions.size();
    }
    private static int getFunction(CommandSourceStack sourceStack, Function<Game2Functions, String> getter, String lang) {
        lang = "commands.xtymegamod.message.map2.game2.function."+lang;
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        String s = getter.apply(data.getGame2Functions());
        if (s == null || s.isEmpty()) s = "null";
        String finalS = s;
        String finalLang = lang;
        sourceStack.sendSuccess(()-> Component.translatable(finalLang, finalS), false);
        return 0;
    }
    private static String formatHomes(List<BlockPos> homes) {
        if (homes.isEmpty()) {
            return "[]";
        }
        return homes.stream().map(BlockPos::toShortString).reduce((a, b) -> a + ", " + b).orElse("[]");
    }
}
