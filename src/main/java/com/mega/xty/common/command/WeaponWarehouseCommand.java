package com.mega.xty.common.command;

import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.warehouse.S2COpenWeaponWarehousePacket;
import com.mega.xty.common.warehouse.WeaponWarehouseItems;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class WeaponWarehouseCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("weaponWarehouse")
                .then(Commands.literal("open")
                        .executes(context -> openSelf(context.getSource()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(stack -> stack.hasPermission(2))
                                .executes(context -> openTarget(context.getSource(), EntityArgument.getPlayer(context, "target")))
                        )
                )
                .then(Commands.literal("apply")
                        .then(Commands.argument("loadout", IntegerArgumentType.integer(1, WeaponWarehouseItems.LOADOUT_COUNT))
                                .executes(context -> applyLoadout(context.getSource(), IntegerArgumentType.getInteger(context, "loadout")))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .requires(stack -> stack.hasPermission(2))
                                        .executes(context -> applyLoadout(EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "loadout")))
                                )
                        )
                );
    }

    private static int openSelf(CommandSourceStack sourceStack) {
        if (!sourceStack.isPlayer()) {
            return 0;
        }
        ServerPlayer player = sourceStack.getPlayer();
        NetworkHandler.sendToPlayer(new S2COpenWeaponWarehousePacket(), player);
        return 1;
    }

    private static int openTarget(CommandSourceStack sourceStack, ServerPlayer target) {
        NetworkHandler.sendToPlayer(new S2COpenWeaponWarehousePacket(), target);
        sourceStack.sendSuccess(() -> Component.translatable("commands.xtymegamod.message.weapon_warehouse.open", target.getDisplayName()), false);
        return 1;
    }

    private static int applyLoadout(CommandSourceStack sourceStack, int loadout) {
        if (!sourceStack.isPlayer()) {
            return 0;
        }
        return applyLoadout(sourceStack.getPlayer(), loadout);
    }

    private static int applyLoadout(ServerPlayer player, int loadout) {
        CommonProxy.getWeaponWarehouseCap(player).ifPresent(cap -> {
            cap.setSelectedWarehouseLoadout(loadout - 1);
            cap.applySelectedWarehouseLoadout(player);
        });
        return 1;
    }
}
