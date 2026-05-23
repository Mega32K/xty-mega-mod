package com.mega.map.common.command;

import com.mega.map.proxy.CommonProxy;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;

public class InteractionCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("interaction")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.literal("tooltip")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", ComponentArgument.textComponent())
                                                .executes(context -> setTooltip(context.getSource(), EntityArgument.getEntity(context, "target"), ComponentArgument.getComponent(context, "value")))
                                        )
                                )
                                .then(Commands.literal("get")
                                        .executes(context -> getTooltip(context.getSource(), EntityArgument.getEntity(context, "target")))
                                )
                        )
                );
    }
    private static int setTooltip(CommandSourceStack stack, Entity entity, Component component) {
        if (entity instanceof Interaction interaction) {
            CommonProxy.getInteractionCap(interaction).ifPresent(cap -> {
                cap.setTooltip(component);
                cap.tooltip().ifPresent(tooltip -> stack.sendSuccess(()-> tooltip, false));
            });
            return 1;
        }
        return 0;
    }
    private static int getTooltip(CommandSourceStack stack, Entity entity) {
        if (entity instanceof Interaction interaction) {
            CommonProxy.getInteractionCap(interaction).ifPresent(cap -> {
                cap.tooltip().ifPresent(tooltip -> stack.sendSuccess(()-> tooltip, false));
            });
            return 1;
        }
        return 0;
    }
}
