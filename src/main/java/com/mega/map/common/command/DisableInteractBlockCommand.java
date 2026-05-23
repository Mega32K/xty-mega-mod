package com.mega.map.common.command;

import com.mega.map.common.data.map1.Game2SavedData;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class DisableInteractBlockCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("interact")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.literal("block")
                        .then(Commands.literal("disable")
                                .executes(context -> {
                                    Game2SavedData.getInstance(context.getSource().getServer()).setDisableBlockInteraction(true);
                                    return 0;
                                })
                        )
                        .then(Commands.literal("enable")
                                .executes(context -> {
                                    Game2SavedData.getInstance(context.getSource().getServer()).setDisableBlockInteraction(false);
                                    return 0;
                                })
                        )
                );
    }
}
