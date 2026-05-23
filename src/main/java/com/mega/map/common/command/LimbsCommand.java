package com.mega.map.common.command;

import com.mega.map.common.capability.Limbs;
import com.mega.map.common.command.argument.LimbArgumentType;
import com.mega.map.proxy.CommonProxy;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

public class LimbsCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("limbs")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("limb", LimbArgumentType.limb())
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(context -> set(context.getSource(), EntityArgument.getPlayer(context, "target"), LimbArgumentType.getLimb(context, "limb"), BoolArgumentType.getBool(context, "value")))
                                )
                                .executes(context -> get(context.getSource(), EntityArgument.getPlayer(context, "target"), LimbArgumentType.getLimb(context, "limb")))
                        )
                );


    }

    private static int set(CommandSourceStack sender, ServerPlayer player, Limbs limbs, boolean disable) {
        CommonProxy.getXtyCap(player).ifPresent(capability -> capability.setLimbDisabled(limbs, disable));
        return disable ? 1 : 0;
    }
    private static int get(CommandSourceStack sender, ServerPlayer player, Limbs limbs) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        CommonProxy.getXtyCap(player).ifPresent(capability -> atomicBoolean.set(capability.isLimbDisabled(limbs)));
        return atomicBoolean.get() ? 1 : 0;
    }
}
