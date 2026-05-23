package com.mega.map.common.command.map2;

import com.mega.map.common.capability.FpsCapability;
import com.mega.map.common.data.map2.Game2SavedData;
import com.mega.map.common.options.map2game2.Game2ClientOptions;
import com.mega.map.common.options.map2game2.Game2ServerOptions;
import com.mega.map.proxy.CommonProxy;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class Map2Game2OptionsCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("options")
                .then(registerServer())
                .then(registerClient());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerServer() {
        return Commands.literal("server")
                .then(Commands.literal("match")
                        .then(integerServerOption("maxWins", "最大胜局数", source -> source.getServerOptions().getMatch().getMaxWins(), (source, value) -> source.getServerOptions().getMatch().setMaxWins(value)))
                        .then(secondsServerOption("roundStartLockSeconds", "回合开始锁定秒数", source -> source.getServerOptions().getMatch().getRoundStartLockTicks(), (source, value) -> source.getServerOptions().getMatch().setRoundStartLockTicks(value)))
                        .then(secondsServerOption("nextRoundDelaySeconds", "下一回合延迟秒数", source -> source.getServerOptions().getMatch().getNextRoundDelayTicks(), (source, value) -> source.getServerOptions().getMatch().setNextRoundDelayTicks(value)))
                        .then(integerServerOption("roundStartHealth", "回合开始生命值", source -> source.getServerOptions().getMatch().getRoundStartHealth(), (source, value) -> source.getServerOptions().getMatch().setRoundStartHealth(value))))
                .then(Commands.literal("bomb")
                        .then(secondsServerOption("countdownSeconds", "炸弹倒计时秒数", source -> source.getServerOptions().getBomb().getCountdownTicks(), (source, value) -> source.getServerOptions().getBomb().setCountdownTicks(value)))
                        .then(secondsServerOption("plantSeconds", "安包耗时秒数", source -> source.getServerOptions().getBomb().getPlantDurationTicks(), (source, value) -> source.getServerOptions().getBomb().setPlantDurationTicks(value)))
                        .then(secondsServerOption("defuseSeconds", "拆包耗时秒数", source -> source.getServerOptions().getBomb().getDefuseDurationTicks(), (source, value) -> source.getServerOptions().getBomb().setDefuseDurationTicks(value)))
                        .then(doubleServerOption("plantSiteDistance", "安包判定距离", source -> source.getServerOptions().getBomb().getPlantSiteDistance(), (source, value) -> source.getServerOptions().getBomb().setPlantSiteDistance(value)))
                        .then(doubleServerOption("defuseDistance", "拆包判定距离", source -> source.getServerOptions().getBomb().getDefuseDistance(), (source, value) -> source.getServerOptions().getBomb().setDefuseDistance(value))))
                .then(Commands.literal("loadout")
                        .then(booleanServerOption("clearDroppedItemsOnNewRound", "新回合清理掉落物", source -> source.getServerOptions().getLoadout().isClearDroppedItemsOnNewRound(), (source, value) -> source.getServerOptions().getLoadout().setClearDroppedItemsOnNewRound(value)))
                        .then(booleanServerOption("giveBlueDefuseKit", "蓝队自动补 C4 钳", source -> source.getServerOptions().getLoadout().isGiveBlueDefuseKit(), (source, value) -> source.getServerOptions().getLoadout().setGiveBlueDefuseKit(value)))
                        .then(booleanServerOption("giveBlueCreativeAmmoBox", "蓝队补全类型创造弹药盒", source -> source.getServerOptions().getLoadout().isGiveBlueCreativeAmmoBox(), (source, value) -> source.getServerOptions().getLoadout().setGiveBlueCreativeAmmoBox(value)))
                        .then(booleanServerOption("fillBlueGunAmmo", "蓝队枪械自动满弹", source -> source.getServerOptions().getLoadout().isFillBlueGunAmmo(), (source, value) -> source.getServerOptions().getLoadout().setFillBlueGunAmmo(value)))
                        .then(booleanServerOption("giveRedBomb", "红队自动发 C4", source -> source.getServerOptions().getLoadout().isGiveRedBomb(), (source, value) -> source.getServerOptions().getLoadout().setGiveRedBomb(value)))
                        .then(booleanServerOption("equipRedNanosuit", "红队自动穿纳米服", source -> source.getServerOptions().getLoadout().isEquipRedNanosuit(), (source, value) -> source.getServerOptions().getLoadout().setEquipRedNanosuit(value)))
                        .then(integerServerOption("blueArmorDurability", "蓝队护甲耐久", source -> source.getServerOptions().getLoadout().getBlueArmorDurability(), (source, value) -> source.getServerOptions().getLoadout().setBlueArmorDurability(value))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerClient() {
        return Commands.literal("client")
                .then(Commands.literal("hud")
                        .then(booleanClientOption("bombCountdownPrompt", "炸弹倒计时提示", options -> options.hud().showBombCountdownPrompt(), (options, value) -> options.hud().setShowBombCountdownPrompt(value)))
                        .then(booleanClientOption("bombProgressBar", "安包拆包进度条", options -> options.hud().showBombProgressBar(), (options, value) -> options.hud().setShowBombProgressBar(value)))
                        .then(booleanClientOption("roundStartPrompt", "回合开始提示", options -> options.hud().showRoundStartPrompt(), (options, value) -> options.hud().setShowRoundStartPrompt(value)))
                        .then(booleanClientOption("roundResultOverlay", "回合结果提示", options -> options.hud().showRoundResultOverlay(), (options, value) -> options.hud().setShowRoundResultOverlay(value)))
                        .then(booleanClientOption("roundMvpOverlay", "MVP 提示", options -> options.hud().showRoundMvpOverlay(), (options, value) -> options.hud().setShowRoundMvpOverlay(value))))
                .then(Commands.literal("visual")
                        .then(booleanClientOption("aspect43", "4:3 拉伸", options -> options.visual().useAspect43(), (options, value) -> options.visual().setUseAspect43(value)))
                        .then(booleanClientOption("roundStartPostEffect", "回合开始后处理", options -> options.visual().useRoundStartPostEffect(), (options, value) -> options.visual().setUseRoundStartPostEffect(value)))
                        .then(booleanClientOption("deadPostEffect", "死亡后处理", options -> options.visual().useDeadPostEffect(), (options, value) -> options.visual().setUseDeadPostEffect(value)))
                        .then(booleanClientOption("deathCamera", "死亡镜头动画", options -> options.visual().useDeathCamera(), (options, value) -> options.visual().setUseDeathCamera(value)))
                        .then(booleanClientOption("c4SpectateCamera", "C4 旁观镜头", options -> options.visual().useC4SpectateCamera(), (options, value) -> options.visual().setUseC4SpectateCamera(value))))
                .then(Commands.literal("audio")
                        .then(booleanClientOption("bombBeep", "炸弹滴滴声", options -> options.audio().playBombBeep(), (options, value) -> options.audio().setPlayBombBeep(value)))
                        .then(booleanClientOption("roundResultSound", "回合结果音效", options -> options.audio().playRoundResultSound(), (options, value) -> options.audio().setPlayRoundResultSound(value))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> integerServerOption(String name, String displayName, ToIntFunction<ServerOptionSource> getter, BiConsumer<ServerOptionSource, Integer> setter) {
        return Commands.literal(name)
                .then(Commands.literal("set")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    ServerOptionSource source = new ServerOptionSource(context);
                                    setter.accept(source, value);
                                    source.commit();
                                    int current = getter.applyAsInt(source);
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.server.set", displayName, current), false);
                                    return current;
                                })))
                .then(Commands.literal("get")
                        .executes(context -> {
                            ServerOptionSource source = new ServerOptionSource(context);
                            int value = getter.applyAsInt(source);
                            context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.server.get", displayName, value), false);
                            return value;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> secondsServerOption(String name, String displayName, ToIntFunction<ServerOptionSource> tickGetter, BiConsumer<ServerOptionSource, Integer> tickSetter) {
        return Commands.literal(name)
                .then(Commands.literal("set")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    int seconds = IntegerArgumentType.getInteger(context, "value");
                                    ServerOptionSource source = new ServerOptionSource(context);
                                    tickSetter.accept(source, seconds * 20);
                                    source.commit();
                                    int current = tickGetter.applyAsInt(source) / 20;
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.server.set", displayName, current), false);
                                    return current;
                                })))
                .then(Commands.literal("get")
                        .executes(context -> {
                            ServerOptionSource source = new ServerOptionSource(context);
                            int value = tickGetter.applyAsInt(source) / 20;
                            context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.server.get", displayName, value), false);
                            return value;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> doubleServerOption(String name, String displayName, ToDoubleFunction<ServerOptionSource> getter, BiConsumer<ServerOptionSource, Double> setter) {
        return Commands.literal(name)
                .then(Commands.literal("set")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0D))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    ServerOptionSource source = new ServerOptionSource(context);
                                    setter.accept(source, value);
                                    source.commit();
                                    double current = getter.applyAsDouble(source);
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.server.set", displayName, current), false);
                                    return (int) Math.round(current);
                                })))
                .then(Commands.literal("get")
                        .executes(context -> {
                            ServerOptionSource source = new ServerOptionSource(context);
                            double value = getter.applyAsDouble(source);
                            context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.server.get", displayName, value), false);
                            return (int) Math.round(value);
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> booleanServerOption(String name, String displayName, Function<ServerOptionSource, Boolean> getter, BiConsumer<ServerOptionSource, Boolean> setter) {
        return Commands.literal(name)
                .then(Commands.literal("set")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean value = BoolArgumentType.getBool(context, "value");
                                    ServerOptionSource source = new ServerOptionSource(context);
                                    setter.accept(source, value);
                                    source.commit();
                                    boolean current = getter.apply(source);
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.server.set", displayName, current), false);
                                    return current ? 1 : 0;
                                })))
                .then(Commands.literal("get")
                        .executes(context -> {
                            ServerOptionSource source = new ServerOptionSource(context);
                            boolean value = getter.apply(source);
                            context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.server.get", displayName, value), false);
                            return value ? 1 : 0;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> booleanClientOption(String name, String displayName, Function<Game2ClientOptions, Boolean> getter, BiConsumer<Game2ClientOptions, Boolean> setter) {
        return Commands.literal(name)
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(context -> {
                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
                                            boolean value = BoolArgumentType.getBool(context, "value");
                                            int changed = 0;
                                            for (ServerPlayer player : players) {
                                                Optional<FpsCapability> capability = CommonProxy.getFPSCap(player).resolve();
                                                if (capability.isPresent()) {
                                                    setter.accept(capability.get().getGame2ClientOptions(), value);
                                                    changed++;
                                                }
                                            }
                                            int count = changed;
                                            context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.client.set", count, displayName, value), false);
                                            return changed;
                                        }))))
                .then(Commands.literal("get")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                    Optional<FpsCapability> capability = CommonProxy.getFPSCap(player).resolve();
                                    boolean value = capability.map(FpsCapability::getGame2ClientOptions).map(getter::apply).orElse(false);
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.options.client.get", player.getGameProfile().getName(), displayName, value), false);
                                    return value ? 1 : 0;
                                })));
    }

    private static final class ServerOptionSource {
        private final Game2SavedData savedData;
        private final Game2ServerOptions serverOptions;

        private ServerOptionSource(CommandContext<CommandSourceStack> context) {
            this.savedData = Game2SavedData.getInstance(context.getSource().getServer());
            this.serverOptions = this.savedData.getServerOptions();
        }

        private Game2ServerOptions getServerOptions() {
            return this.serverOptions;
        }

        private void commit() {
            this.savedData.onServerOptionsUpdated();
        }
    }
}
