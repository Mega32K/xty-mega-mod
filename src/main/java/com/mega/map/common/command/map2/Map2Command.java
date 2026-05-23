package com.mega.map.common.command.map2;

import com.mega.endinglib.api.client.cmc.LoreHelper;
import com.mega.map.common.data.map2.Map2SavedData;
import com.mega.map.common.data.map2.Map2Functions;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.s2c.map2.S2CPlayerRenamePacket;
import com.mega.map.common.network.s2c.map2.S2CMap2CountdownPacket;
import com.mega.map.proxy.CommonProxy;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Map2Command {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (p_137719_, p_137720_) -> {
        ServerFunctionManager serverfunctionmanager = p_137719_.getSource().getServer().getFunctions();
        return SharedSuggestionProvider.suggestResource(serverfunctionmanager.getFunctionNames(), p_137720_);
    };
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("map2")
                .requires(stack -> stack.hasPermission(1))
                .then(Commands.literal("start")
                        .executes(context -> start(context.getSource()))
                )
                .then(Commands.literal("stop")
                        .executes(context -> stop(context.getSource()))
                )
                .then(Commands.literal("mode")
                        .then(Commands.literal("setTeam")
                                .executes(context -> {
                                    Map2SavedData data = Map2SavedData.getInstance(context.getSource().getServer());
                                    data.setMode(true);
                                    return 0;
                                })
                        )
                        .then(Commands.literal("setPersonal")
                                .executes(context -> {
                                    Map2SavedData data = Map2SavedData.getInstance(context.getSource().getServer());
                                    data.setMode(false);
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("scoreOverlay")
                        .then(Commands.literal("leftKillCount")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.literal("setFromResult")
                                                .redirect(dispatcher.getRoot(), context -> storeFirstKillCountFrom(context.getSource(), EntityArgument.getPlayer(context, "player")))
                                        )
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                        .executes(context -> setFirstKillCount(EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "value")))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("rightKillCount")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.literal("setFromResult")
                                                .redirect(dispatcher.getRoot(), context -> storeSecKillCountFrom(context.getSource(), EntityArgument.getPlayer(context, "player")))
                                        )
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                        .executes(context -> setSecKillCount(EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "value")))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("setVisible")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(context -> setScoreOverlayVisible(context.getSource(), BoolArgumentType.getBool(context, "value")))
                                )
                        )
                        .then(Commands.literal("team")
                                .then(Commands.literal("setScoreVisible")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setTeamScoreVisible(context.getSource(), BoolArgumentType.getBool(context, "value")))
                                        )
                                )
                                .then(Commands.literal("setRedScore")
                                        .then(Commands.literal("setFromResult")
                                                .redirect(dispatcher.getRoot(), context -> storeRedScoreFrom(context.getSource()))
                                        )
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                        .executes(context -> setRedScore(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                                )
                                        )
                                )
                                .then(Commands.literal("setBlueScore")
                                        .then(Commands.literal("setFromResult")
                                                .redirect(dispatcher.getRoot(), context -> storeBlueScoreFrom(context.getSource()))
                                        )
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                        .executes(context -> setBlueScore(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                                )
                                        )
                                )
                                .then(Commands.literal("setRedWins")
                                        .then(Commands.literal("setFromResult")
                                                .redirect(dispatcher.getRoot(), context -> storeRedWinsFrom(context.getSource()))
                                        )
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                        .executes(context -> setRedWins(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                                )
                                        )
                                )
                                .then(Commands.literal("setBlueWins")
                                        .then(Commands.literal("setFromResult")
                                                .redirect(dispatcher.getRoot(), context -> storeBlueWinsFrom(context.getSource()))
                                        )
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                        .executes(context -> setBlueWins(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("countdown")
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                        .executes(context -> setCountdown(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                )
                        )
                        .then(Commands.literal("get")
                                .executes(context -> getCountdown(context.getSource()))
                        )
                )
                .then(Commands.literal("maxWins")
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(context -> setMaxWins(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                )
                        )
                        .then(Commands.literal("get")
                                .executes(context -> getMaxWins(context.getSource()))
                        )
                )
                .then(Commands.literal("playerCountNeed")
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            int v = IntegerArgumentType.getInteger(context, "value");
                                            Map2SavedData data = Map2SavedData.getInstance(context.getSource().getServer());
                                            data.setPlayerCountNeed(v);
                                            return v;
                                        })
                                )
                        )
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    Map2SavedData data = Map2SavedData.getInstance(context.getSource().getServer());
                                    context.getSource().sendSuccess(() -> LoreHelper.number(data.getPlayerCountNeed(), ChatFormatting.GOLD), false);
                                    return data.getPlayerCountNeed();
                                })
                        )
                )
                .then(Commands.literal("functions")
                        .then(Commands.literal("start")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Map2Functions::setStartFunction, "start.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Map2Functions::getStartFunction, "start.get"))
                                )
                        )
                        .then(Commands.literal("stop")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Map2Functions::setStopFunction, "stop.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Map2Functions::getStopFunction, "stop.get"))
                                )
                        )
                        .then(Commands.literal("countdownStop")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Map2Functions::setCountdownStopFunction, "countdown_stop.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Map2Functions::getCountdownStopFunction, "countdown_stop.get"))
                                )
                        )
                )
                .then(Commands.literal("renameRequest")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> renameRequest(EntityArgument.getPlayers(context, "players"), EntityArgument.getPlayer(context, "target")))
                                )
                                .executes(context -> renameRequest(EntityArgument.getPlayers(context, "players")))
                        )
                )
                .then(Commands.literal("points")
                        .then(Commands.literal("a")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> setPA(context.getSource(), BlockPosArgument.getBlockPos(context, "pos")))
                                        )
                                )
                                .then(Commands.literal("clear")
                                        .executes(context -> setPA(context.getSource(), null))
                                )
                        )
                        .then(Commands.literal("b")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> setPB(context.getSource(), BlockPosArgument.getBlockPos(context, "pos")))
                                        )
                                )
                                .then(Commands.literal("clear")
                                        .executes(context -> setPB(context.getSource(), null))
                                )
                        )
                )
                .then(Commands.literal("xaero")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("deathPos")
                                        .then(Commands.literal("set")
                                                .executes(context -> setDeathPos(context.getSource(), EntityArgument.getPlayer(context, "player")))
                                        )
                                        .then(Commands.literal("clear")
                                                .executes(context -> clearDeathPos(EntityArgument.getPlayer(context, "player")))
                                        )
                                )
                                .then(Commands.literal("setDead")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setXaeroDead(EntityArgument.getPlayer(context, "player"), BoolArgumentType.getBool(context, "value")))
                                        )
                                )
                        )
                )
                .then(Commands.literal("setTextTip")
                        .then(Commands.argument("value", ComponentArgument.textComponent())
                                .executes(context -> setTextTip(context.getSource(), ComponentArgument.getComponent(context, "value")))
                        )
                );
    }
    private static int setXaeroDead(ServerPlayer player, boolean value) {
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            cap.setXaeroDead(value);
        });
        return 0;
    }
    private static int setDeathPos(CommandSourceStack sourceStack, ServerPlayer player) {
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            cap.setLastDeathPos(sourceStack.getPosition().toVector3f());
        });
        return 0;
    }
    private static int clearDeathPos(ServerPlayer player) {
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            cap.setLastDeathPos(null);
        });
        return 0;
    }
    private static int setPA(CommandSourceStack sourceStack, BlockPos pos) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setPointA(pos);
        return 0;
    }
    private static int setPB(CommandSourceStack sourceStack, BlockPos pos) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setPointB(pos);
        return 0;
    }
    private static int renameRequest(Collection<ServerPlayer> players, ServerPlayer target) {
        for (ServerPlayer sp : players)
            NetworkHandler.sendToPlayer(new S2CPlayerRenamePacket(target.getUUID()), sp);
        return players.size();
    }
    private static int renameRequest(Collection<ServerPlayer> players) {
        for (ServerPlayer sp : players)
            NetworkHandler.sendToPlayer(new S2CPlayerRenamePacket(sp.getUUID()), sp);
        return players.size();
    }
    private static int start(CommandSourceStack sourceStack) {
        for (ServerPlayer sp : sourceStack.getServer().getPlayerList().getPlayers()) {
            if (!sp.isSpectator()) {
                CommonProxy.getMap2Cap(sp).ifPresent(cap  -> {
                    cap.setNeedStart(true);
                });
            }
        }
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setStopped(false);
        Entity entity = sourceStack.getEntity();
        if (entity != null) {
            CommandSourceStack sourceStack2 = sourceStack.withSuppressedOutput().withMaximumPermission(2);
            MinecraftServer server = sourceStack2.getServer();
            String func = data.getMap2Functions().getStartFunction();
            if (func != null && !func.isEmpty()) {
                server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f -> server.getFunctions().execute(f, sourceStack2));
            }
        }
        return 0;
    }
    private static int stop(CommandSourceStack sourceStack) {
        for (ServerPlayer sp : sourceStack.getServer().getPlayerList().getPlayers()) {
            if (!sp.isSpectator()) {
                CommonProxy.getMap2Cap(sp).ifPresent(cap  -> {
                    cap.setNeedStart(false);
                });
            }
        }
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setStopped(true);
        Entity entity = sourceStack.getEntity();
        if (entity != null) {
            CommandSourceStack sourceStack2 = sourceStack.withSuppressedOutput().withMaximumPermission(2);
            MinecraftServer server = sourceStack2.getServer();
            String func = data.getMap2Functions().getStopFunction();
            if (func != null && !func.isEmpty()) {
                server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f-> server.getFunctions().execute(f, sourceStack2));
            }
        }
        return 1;
    }
    private static CommandSourceStack storeFirstKillCountFrom(CommandSourceStack sourceStack, ServerPlayer player) {
        return sourceStack.withCallback((s, success, rV) -> {
            CommonProxy.getMap2Cap(player).ifPresent(map2Capability -> {
                map2Capability.set1KillCount(rV);
            });
        });
    }
    private static int setFirstKillCount(ServerPlayer player, int value) {
        CommonProxy.getMap2Cap(player).ifPresent(map2Capability -> {
            map2Capability.set1KillCount(value);
        });
        return value;
    }
    private static CommandSourceStack storeSecKillCountFrom(CommandSourceStack sourceStack, ServerPlayer player) {
        return sourceStack.withCallback((s, success, rV) -> {
            CommonProxy.getMap2Cap(player).ifPresent(map2Capability -> {
                map2Capability.set2KillCount(rV);
            });
        });
    }
    private static int setSecKillCount(ServerPlayer player, int value) {
        CommonProxy.getMap2Cap(player).ifPresent(map2Capability -> {
            map2Capability.set2KillCount(value);
        });
        return value;
    }
    private static int setCountdown(CommandSourceStack sourceStack, int countdown) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setCountdown(countdown * 20);
        NetworkHandler.sendToAll(new S2CMap2CountdownPacket(data.getCountdown()));
        return countdown;
    }
    private static int getCountdown(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        int i = data.getCountdown();
        sourceStack.sendSuccess(()-> Component.translatable("commands.megamod.message.map2.countdown.get", LoreHelper.number(i, ChatFormatting.GOLD)), false);
        return i;
    }
    private static int setMaxWins(CommandSourceStack sourceStack, int value) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        data.setMaxWins(value);
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.map2.max_wins.set", value), false);
        return value;
    }
    private static int getMaxWins(CommandSourceStack sourceStack) {
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        int value = data.getMaxWins();
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.map2.max_wins.get", value), false);
        return value;
    }
    private static CommandSourceStack storeRedScoreFrom(CommandSourceStack sourceStack) {
        return sourceStack.withCallback((s, success, rV) -> {
            Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
            savedData.setRedScore(rV);
        });
    }
    private static int setRedScore(CommandSourceStack sourceStack, int value) {
        Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
        savedData.setRedScore(value);
        return value;
    }
    private static CommandSourceStack storeBlueScoreFrom(CommandSourceStack sourceStack) {
        return sourceStack.withCallback((s, success, rV) -> {
            Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
            savedData.setBlueScore(rV);
        });
    }
    private static int setBlueScore(CommandSourceStack sourceStack, int value) {
        Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
        savedData.setBlueScore(value);
        return value;
    }
    private static CommandSourceStack storeRedWinsFrom(CommandSourceStack sourceStack) {
        return sourceStack.withCallback((s, success, rV) -> {
            Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
            savedData.setRedWins(rV);
        });
    }
    private static int setRedWins(CommandSourceStack sourceStack, int value) {
        Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
        savedData.setRedWins(value);
        return value;
    }
    private static CommandSourceStack storeBlueWinsFrom(CommandSourceStack sourceStack) {
        return sourceStack.withCallback((s, success, rV) -> {
            Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
            savedData.setBlueWins(rV);
        });
    }
    private static int setBlueWins(CommandSourceStack sourceStack, int value) {
        Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
        savedData.setBlueWins(value);
        return value;
    }
    private static int setTeamScoreVisible(CommandSourceStack sourceStack, boolean visible) {
        Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
        savedData.setTeamScoreVisible(visible);
        return visible ? 1 : 0;
    }

    private static int setFunction(CommandSourceStack sourceStack, Collection<CommandFunction> functions, BiConsumer<Map2Functions, String> setter, String lang) {
        lang = "commands.megamod.message.map2.function."+lang;
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        if (functions.isEmpty())
            return 0;
        String function = functions.iterator().next().getId().toString();
        setter.accept(data.getMap2Functions(),function);
        String finalLang = lang;
        sourceStack.sendSuccess(()-> Component.translatable(finalLang, function), false);
        return functions.size();
    }
    private static int getFunction(CommandSourceStack sourceStack, Function<Map2Functions, String> getter, String lang) {
        lang = "commands.megamod.message.map2.function."+lang;
        Map2SavedData data = Map2SavedData.getInstance(sourceStack.getServer());
        String s = getter.apply(data.getMap2Functions());
        if (s == null || s.isEmpty()) s = "null";
        String finalS = s;
        String finalLang = lang;
        sourceStack.sendSuccess(()-> Component.translatable(finalLang, finalS), false);
        return 0;
    }
    private static int setScoreOverlayVisible(CommandSourceStack sourceStack, boolean visible) {
        Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
        savedData.setScoreOverlayVisible(visible);
        return visible ? 1 : 0;
    }
    private static int setTextTip(CommandSourceStack sourceStack, Component component) {
        Map2SavedData savedData = Map2SavedData.getInstance(sourceStack.getServer());
        if (component.getString().isEmpty())
            savedData.setRightTopText(null);
        else savedData.setRightTopText(component);
        return 0;
    }
}
