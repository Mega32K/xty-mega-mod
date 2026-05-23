package com.mega.map.common.command;

import com.mega.endinglib.api.client.cmc.LoreHelper;
import com.mega.map.proxy.CommonProxy;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameInvulCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("gameInvul")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(context -> set(context.getSource(), EntityArgument.getPlayers(context, "targets"), BoolArgumentType.getBool(context, "value")))
                        )
                        .executes(context -> get(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                );


    }

    private static int set(CommandSourceStack sender, Collection<ServerPlayer> players, boolean disable) {
        if (players.size() == 1) {
            ServerPlayer player = players.iterator().next();
            CommonProxy.getXtyCap(player).ifPresent(capability -> {
                capability.setGameInvulnerable(disable);
                sender.sendSuccess(()-> Component.translatable("commands.megamod.message.game_invul.set.single", player.getDisplayName(), disable), false);
            });
        } else {
            for (ServerPlayer player : players) {
                CommonProxy.getXtyCap(player).ifPresent(capability -> {
                    capability.setGameInvulnerable(disable);
                });
            }
            sender.sendSuccess(()-> Component.translatable("commands.megamod.message.game_invul.set.multiple", LoreHelper.number(players.size(), ChatFormatting.GOLD), disable), false);
        }
        return players.size();
    }
    private static int get(CommandSourceStack sender, Collection<ServerPlayer> players) {
        if (players.size() != 1) return 0;
        ServerPlayer player = players.iterator().next();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        CommonProxy.getXtyCap(player).ifPresent(capability -> atomicBoolean.set(capability.isGameInvul()));
        sender.sendSuccess(()-> Component.translatable("commands.megamod.message.game_invul.get", player.getDisplayName(), LoreHelper.bool(atomicBoolean.get())), false);
        return atomicBoolean.get() ? 1 : 0;
    }
}
