package com.mega.map.common.command.map2;

import com.mega.map.common.data.map2.Game2SavedData;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Game2WarehouseMeleeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("warehouseMelee")
                .then(Commands.literal("add")
                        .executes(context -> add(context.getSource(), null))
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(context -> add(context.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "player")))
                        )
                )
                .then(Commands.literal("remove")
                        .executes(context -> remove(context.getSource(), null))
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(context -> remove(context.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "player")))
                        )
                )
                .then(Commands.literal("clear")
                        .executes(context -> clear(context.getSource()))
                )
                .then(Commands.literal("get")
                        .executes(context -> get(context.getSource()))
                );
    }

    private static int add(CommandSourceStack sourceStack, ServerPlayer target) {
        ServerPlayer player = target != null ? target : sourceStack.getPlayer();
        if (player == null) {
            return 0;
        }
        ItemStack stack = player.getMainHandItem().copy();
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        List<ItemStack> stacks = new ArrayList<>(data.getExtraWarehouseMeleeStacks());
        boolean exists = stacks.stream().anyMatch(existing -> ItemStack.isSameItemSameTags(existing, stack));
        if (!exists) {
            stacks.add(stack);
            data.setExtraWarehouseMeleeStacks(stacks);
        }
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.warehouse_melee.add", stack.getHoverName()), false);
        return stacks.size();
    }

    private static int remove(CommandSourceStack sourceStack, ServerPlayer target) {
        ServerPlayer player = target != null ? target : sourceStack.getPlayer();
        if (player == null) {
            return 0;
        }
        ItemStack stack = player.getMainHandItem().copy();
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        List<ItemStack> stacks = new ArrayList<>(data.getExtraWarehouseMeleeStacks());
        stacks.removeIf(existing -> ItemStack.isSameItemSameTags(existing, stack));
        data.setExtraWarehouseMeleeStacks(stacks);
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.warehouse_melee.remove", stack.getHoverName()), false);
        return stacks.size();
    }

    private static int clear(CommandSourceStack sourceStack) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        data.setExtraWarehouseMeleeStacks(List.of());
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.warehouse_melee.clear"), false);
        return 0;
    }

    private static int get(CommandSourceStack sourceStack) {
        Game2SavedData data = Game2SavedData.getInstance(sourceStack.getServer());
        String value = data.getExtraWarehouseMeleeStacks().isEmpty()
                ? "[]"
                : data.getExtraWarehouseMeleeStacks().stream().map(stack -> stack.getHoverName().getString()).reduce((a, b) -> a + ", " + b).orElse("[]");
        sourceStack.sendSuccess(() -> Component.translatable("commands.megamod.message.map2.game2.warehouse_melee.get", value), false);
        return data.getExtraWarehouseMeleeStacks().size();
    }
}
