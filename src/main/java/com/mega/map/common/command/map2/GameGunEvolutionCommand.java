package com.mega.map.common.command.map2;

import com.mega.map.common.data.map2.Game1Functions;
import com.mega.map.common.data.map2.Game1SavedData;
import com.mega.map.proxy.CommonProxy;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GameGunEvolutionCommand {
    private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((p_138534_, p_138535_) -> Component.translatable("commands.scoreboard.players.get.null", p_138534_, p_138535_));
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (p_137719_, p_137720_) -> {
        ServerFunctionManager serverfunctionmanager = p_137719_.getSource().getServer().getFunctions();
        return SharedSuggestionProvider.suggestResource(serverfunctionmanager.getFunctionNames(), p_137720_);
    };
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("map2game1")
                .requires(stack -> stack.hasPermission(1))
                .then(Commands.literal("start")
                        .executes(context -> start(context.getSource()))
                )
                .then(Commands.literal("stop")
                        .executes(context -> stop(context.getSource()))
                )
                .then(Commands.literal("isStopped")
                        .executes(context -> Game1SavedData.getInstance(context.getSource().getServer()).isStopped() ? 0 : 1)
                )
                .then(Commands.literal("evolution")
                        //设置某个索引处的所有武器
                        .then(Commands.literal("weapon")
                                .then(Commands.argument("index", IntegerArgumentType.integer(0, 12))
                                        .then(Commands.literal("copyFrom")
                                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(context -> copyEvolutionFromChest(context.getSource(), IntegerArgumentType.getInteger(context, "index"), BlockPosArgument.getBlockPos(context, "pos")))
                                                )
                                        )
                                        .then(Commands.literal("clear")
                                                .executes(context -> clearEvolution(context.getSource(), IntegerArgumentType.getInteger(context, "index")))
                                        )
                                )
                        )
                        .then(Commands.literal("player")
                                //每个玩家根据当前武器进化数据生成随机索引
                                .then(Commands.literal("setRandomIndex")
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context -> spawnRandomPlayerSelector(context.getSource(), EntityArgument.getPlayer(context, "target")))
                                        )
                                )
                        )
                )
                .then(Commands.literal("function")
                        .then(Commands.literal("onPlayerDeath")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game1Functions::setOnPlayerDeathFunction, "on_player_death.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game1Functions::getOnPlayerDeathFunction, "on_player_death.get"))
                                )
                        )
                        .then(Commands.literal("start")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game1Functions::setStartFunction, "start.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game1Functions::getStartFunction, "start.get"))
                                )
                        )
                        .then(Commands.literal("stop")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("function", FunctionArgument.functions())
                                                .suggests(SUGGEST_FUNCTION)
                                                .executes(context -> setFunction(context.getSource(), FunctionArgument.getFunctions(context, "function"), Game1Functions::setStopFunction, "stop.set"))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getFunction(context.getSource(), Game1Functions::getStopFunction, "stop.get"))
                                )
                        )
                )
                .then(Commands.literal("playerData")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("evolutionIndex")
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> setEvolutionIndex(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "value")))
                                                )
                                        )
                                        .then(Commands.literal("get")
                                                .executes(context -> getEvolutionIndex(context.getSource(), EntityArgument.getPlayer(context, "player")))
                                        )
                                )
                                .then(Commands.literal("evolutionKillCount")
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> setEvolutionKillCount(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "value")))
                                                )
                                        )
                                        .then(Commands.literal("get")
                                                .executes(context -> getEvolutionKillCount(context.getSource(), EntityArgument.getPlayer(context, "player")))
                                        )
                                )
                        )
                );
    }
    private static int start(CommandSourceStack sourceStack) {
        Game1SavedData data = Game1SavedData.getInstance(sourceStack.getServer());
        data.setStopped(false);
        Entity entity = sourceStack.getEntity();
        if (entity != null) {
            CommandSourceStack sourceStack2 = sourceStack.withSuppressedOutput().withMaximumPermission(2);
            MinecraftServer server = sourceStack2.getServer();
            String func = data.getGame1Functions().getStartFunction();
            if (func != null && !func.isEmpty()) {
                server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f -> server.getFunctions().execute(f, sourceStack2));
            }
        }
        return 1;
    }
    private static int stop(CommandSourceStack sourceStack) {
        Game1SavedData data = Game1SavedData.getInstance(sourceStack.getServer());
        data.setStopped(true);
        Entity entity = sourceStack.getEntity();
        if (entity != null) {
            CommandSourceStack sourceStack2 = sourceStack.withSuppressedOutput().withMaximumPermission(2);
            MinecraftServer server = sourceStack2.getServer();
            String func = data.getGame1Functions().getStopFunction();
            if (func != null && !func.isEmpty()) {
                server.getFunctions().get(ResourceLocation.parse(func)).ifPresent(f-> server.getFunctions().execute(f, sourceStack2));
            }
        }
        return 1;
    } 
    private static int setFunction(CommandSourceStack sourceStack, Collection<CommandFunction> functions, BiConsumer<Game1Functions, String> setter, String lang) {
        lang = "commands.megamod.message.map2.game1.function."+lang;
        Game1SavedData data = Game1SavedData.getInstance(sourceStack.getServer());
        if (functions.isEmpty())
            return 0;
        String function = functions.iterator().next().getId().toString();
        setter.accept(data.getGame1Functions(),function);
        String finalLang = lang;
        sourceStack.sendSuccess(()-> Component.translatable(finalLang, function), false);
        return functions.size();
    }
    private static int getFunction(CommandSourceStack sourceStack, Function<Game1Functions, String> getter, String lang) {
        lang = "commands.megamod.message.map2.game1.function."+lang;
        Game1SavedData data = Game1SavedData.getInstance(sourceStack.getServer());
        String s = getter.apply(data.getGame1Functions());
        if (s == null || s.isEmpty()) s = "null";
        String finalS = s;
        String finalLang = lang;
        sourceStack.sendSuccess(()-> Component.translatable(finalLang, finalS), false);
        return 0;
    }
    private static int copyEvolutionFromChest(CommandSourceStack sourceStack, int index, BlockPos pos) {
        ServerLevel serverLevel = sourceStack.getLevel();
        if (serverLevel.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity containerBE) {
            Game1SavedData savedData = Game1SavedData.getInstance(sourceStack.getServer());
            List<ItemStack> stackList = new ObjectArrayList<>();
            for (int i=0;i<containerBE.getContainerSize();i++) {
                ItemStack itemStack = containerBE.getItem(i);
                if (!itemStack.isEmpty()) {
                    stackList.add(itemStack);
                }
            }
            savedData.putEvolutionWeapons(index, stackList);
        }
        return 0;
    }
    private static int clearEvolution(CommandSourceStack sourceStack, int index) {
        Game1SavedData savedData = Game1SavedData.getInstance(sourceStack.getServer());
        savedData.putEvolutionWeapons(index, List.of());
        return 0;
    }
    private static int spawnRandomPlayerSelector(CommandSourceStack sourceStack, ServerPlayer player) {
        Game1SavedData savedData = Game1SavedData.getInstance(sourceStack.getServer());
        savedData.spawnPlayerSelector(player);
        return 0;
    }
    private static int getEvolutionIndex(CommandSourceStack sourceStack, ServerPlayer player) {
        MutableInt mutableInt = new MutableInt(0);
        CommonProxy.getMap2Cap(player).ifPresent(cap -> mutableInt.setValue(cap.getEvolutionIndex()));
        return mutableInt.getValue();
    }
    private static int setEvolutionIndex(CommandSourceStack sourceStack, ServerPlayer player, int value) {
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            cap.setEvolutionIndex(value);
        });
        return value;
    }
    private static int getEvolutionKillCount(CommandSourceStack sourceStack, ServerPlayer player) {
        MutableInt mutableInt = new MutableInt(0);
        CommonProxy.getMap2Cap(player).ifPresent(cap -> mutableInt.setValue(cap.getEvolutionKillCount()));
        return mutableInt.getValue();
    }
    private static int setEvolutionKillCount(CommandSourceStack sourceStack, ServerPlayer player, int value) {
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            cap.setEvolutionKillCount(value);
        });
        return value;
    }
}
