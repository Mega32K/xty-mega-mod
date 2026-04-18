package com.mega.xty.common.command.map2;

import com.mega.xty.common.data.map2.*;
import com.mega.xty.common.data.map2.Game2SavedData;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
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
}
