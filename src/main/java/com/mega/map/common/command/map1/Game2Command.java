package com.mega.map.common.command.map1;

import com.mega.map.common.data.map1.Game2Functions;
import com.mega.map.common.data.map1.Game2SavedData;
import com.mega.map.common.entity.ShadowPlayerEntity;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.s2c.map1.game2.S2CScreenShakePacket;
import com.mega.map.common.network.s2c.map1.game2.S2CSimpleScreenShakePacket;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Game2Command {
    private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((p_138534_, p_138535_) -> Component.translatable("commands.scoreboard.players.get.null", p_138534_, p_138535_));
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (p_137719_, p_137720_) -> {
        ServerFunctionManager serverfunctionmanager = p_137719_.getSource().getServer().getFunctions();
        return SharedSuggestionProvider.suggestResource(serverfunctionmanager.getFunctionNames(), p_137720_);
    };
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("game2")
                .requires(stack -> stack.hasPermission(1))
                .then(Commands.literal("start")
                        .executes(context -> start(context.getSource()))
                )
                .then(Commands.literal("stop")
                        .executes(context -> stop(context.getSource()))
                )
                .then(Commands.literal("startSceneChanging")
                        .executes(context -> startSceneChanging(context.getSource()))
                )
                .then(Commands.literal("stopSceneChanging")
                        .executes(context -> stopSceneChanging(context.getSource()))
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
                        .then(Commands.literal("hurtByEntity")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game2Functions::setHurtByEntityFunction, "hurt_by_entity.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game2Functions::getHurtByEntityFunction, "hurt_by_entity.get"))
                                )
                        )
                        .then(Commands.literal("jump")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game2Functions::setJumpFunction, "jump.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game2Functions::getJumpFunction, "jump.get"))
                                )
                        )
                        .then(Commands.literal("gunShoot")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game2Functions::setGunShootFunction, "jump.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game2Functions::getGunShootFunction, "jump.get"))
                                )
                        )
                )
                .then(Commands.literal("health")
                        .then(Commands.literal("health")
                                .then(Commands.literal("setObjective")
                                        .then(Commands.argument("objective", ObjectiveArgument.objective())
                                                .executes(context -> setHealthObjective(context.getSource(), ObjectiveArgument.getObjective(context, "objective")))
                                        )
                                )
                                .then(Commands.literal("getObjective")
                                        .executes(context -> getHealthObjective(context.getSource()))
                                )
                        )
                        .then(Commands.literal("maxHealth")
                                .then(Commands.literal("setObjective")
                                        .then(Commands.argument("objective", ObjectiveArgument.objective())
                                                .executes(context -> setMaxHealthObjective(context.getSource(), ObjectiveArgument.getObjective(context, "objective")))
                                        )
                                )
                                .then(Commands.literal("getObjective")
                                        .executes(context -> getMaxHealthObjective(context.getSource()))
                                )
                        )
                )
                .then(Commands.literal("shake")
                        .then(Commands.literal("simple")
                                .then(Commands.argument("arrow", Vec2Argument.vec2(false))
                                        .then(Commands.argument("target", ScoreHolderArgument.scoreHolder())
                                                .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                .then(Commands.argument("objective", ObjectiveArgument.objective())
                                                        .then(Commands.argument("scale", FloatArgumentType.floatArg(0))
                                                                .executes(context -> shake2(context.getSource(), Vec2Argument.getVec2(context, "arrow"), ScoreHolderArgument.getName(context, "target"), ObjectiveArgument.getObjective(context, "objective"), FloatArgumentType.getFloat(context, "scale")))
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.argument("start", Vec3Argument.vec3(true))
                                .then(Commands.argument("end", Vec3Argument.vec3(true))
                                        .then(Commands.argument("target", ScoreHolderArgument.scoreHolder())
                                                .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                .then(Commands.argument("objective", ObjectiveArgument.objective())
                                                        .then(Commands.argument("scale", FloatArgumentType.floatArg(0))
                                                                .executes(context -> shake(context.getSource(), Vec3Argument.getCoordinates(context, "start"), Vec3Argument.getCoordinates(context, "end"), ScoreHolderArgument.getName(context, "target"), ObjectiveArgument.getObjective(context, "objective"), FloatArgumentType.getFloat(context, "scale")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("makeShadowPlayer")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    ShadowPlayerEntity shadow = new ShadowPlayerEntity(player);
                                    shadow.setPos(player.position());
                                    player.serverLevel().addFreshEntity(shadow);
                                    return 0;
                                })
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
                server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f-> server.getFunctions().execute(f, sourceStack2));
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
    private static int startSceneChanging(CommandSourceStack sourceStack) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        data.setSceneChanging(true);
        return 1;
    }
    private static int stopSceneChanging(CommandSourceStack sourceStack) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        data.setSceneChanging(false);
        return 1;
    }
    private static int shake2(CommandSourceStack sourceStack, Vec2 arrow, String name, Objective objective, float scale) throws CommandSyntaxException {
        Scoreboard scoreboard = sourceStack.getServer().getScoreboard();
        if (!scoreboard.hasPlayerScore(name, objective)) {
            throw ERROR_NO_VALUE.create(objective.getName(), name);
        } else {
            Score score = scoreboard.getOrCreatePlayerScore(name, objective);
            float power = score.getScore() * 0.001F * scale;
            for (ServerPlayer player : sourceStack.getLevel().players()) {
                NetworkHandler.sendToPlayer(new S2CSimpleScreenShakePacket(arrow.x, arrow.y, power), player);
            }
        }
        return 0;
    }
    private static int shake(CommandSourceStack sourceStack, Coordinates endC, Coordinates startC, String name, Objective objective, float scale) throws CommandSyntaxException {
        Vector3f start = startC.getPosition(sourceStack).toVector3f();
        Vector3f end = endC.getPosition(sourceStack).toVector3f();
        Scoreboard scoreboard = sourceStack.getServer().getScoreboard();
        if (!scoreboard.hasPlayerScore(name, objective)) {
            throw ERROR_NO_VALUE.create(objective.getName(), name);
        } else {
            Score score = scoreboard.getOrCreatePlayerScore(name, objective);
            float power = score.getScore() * 0.001F * scale;
            for (ServerPlayer player : sourceStack.getLevel().players()) {
                NetworkHandler.sendToPlayer(new S2CScreenShakePacket(start, end, power), player);
            }
        }
        return 0;
    }

    private static int setHealthObjective(CommandSourceStack sourceStack, Objective objective) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        data.setHealthScoreObjective(objective);
        sourceStack.sendSuccess(()-> Component.translatable("commands.megamod.message.game2.health.set_objective", objective.getFormattedDisplayName()), false);
        return 0;
    }
    private static int getHealthObjective(CommandSourceStack sourceStack) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        Objective objective = data.getHealthObjective();
        Component name = objective == null ? Component.literal("null").withStyle(ChatFormatting.RED) : objective.getFormattedDisplayName();
        sourceStack.sendSuccess(()-> Component.translatable("commands.megamod.message.game2.health.get_objective", name), false);
        return objective == null ? 0 : 1;
    }
    private static int setMaxHealthObjective(CommandSourceStack sourceStack, Objective objective) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        data.setMaxHealthScoreObjective(objective);
        sourceStack.sendSuccess(()-> Component.translatable("commands.megamod.message.game2.health.max.set_objective", objective.getFormattedDisplayName()), false);
        return 0;
    }
    private static int getMaxHealthObjective(CommandSourceStack sourceStack) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        Objective objective = data.getMaxHealthObjective();
        Component name = objective == null ? Component.literal("null").withStyle(ChatFormatting.RED) : objective.getFormattedDisplayName();
        sourceStack.sendSuccess(()-> Component.translatable("commands.megamod.message.game2.health.max.get_objective", name), false);
        return objective == null ? 0 : 1;
    }
    private static int setFunction(CommandSourceStack sourceStack, Collection<CommandFunction> functions, BiConsumer<Game2Functions, String> setter, String lang) {
        lang = "commands.megamod.message.map1.game2.function."+lang;
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
        lang = "commands.megamod.message.map1.game2.function."+lang;
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        String s = getter.apply(data.getGame2Functions());
        if (s == null || s.isEmpty()) s = "null";
        String finalS = s;
        String finalLang = lang;
        sourceStack.sendSuccess(()-> Component.translatable(finalLang, finalS), false);
        return 0;
    }
}
