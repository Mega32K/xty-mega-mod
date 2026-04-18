package com.mega.xty.common.command;

import com.mega.xty.common.capability.Limbs;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

public class SpinAttackCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("spinAttack")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("duration", IntegerArgumentType.integer(0))
                                .executes(context -> execute(context.getSource(), EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "duration")))
                        )
                );


    }

    private static int execute(CommandSourceStack sender, ServerPlayer player, int duration) {
        player.startAutoSpinAttack(duration);
        return 0;
    }
}
