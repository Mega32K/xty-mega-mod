package com.mega.map.common.command;

import com.mega.map.proxy.CommonProxy;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class NoPhysicsCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("noPhysics")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("duration", IntegerArgumentType.integer(0))
                                .executes(context -> execute(context.getSource(), EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "duration")))
                        )
                );


    }

    private static int execute(CommandSourceStack sender, ServerPlayer player, int duration) {
        CommonProxy.getXtyCap(player).ifPresent(capability -> {
            capability.setNoPhysicsTime(duration);
        });
        return 0;
    }
}
