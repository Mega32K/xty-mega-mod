package com.mega.xty.common.command;

import com.mega.endinglib.common.config.CommandConfig;
import com.mega.xty.common.command.map1.Game2Command;
import com.mega.xty.common.command.map2.GameGhostModeCommand;
import com.mega.xty.common.command.map2.GameGunEvolutionCommand;
import com.mega.xty.common.command.map2.Map2Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class XtyCommands {
    @SubscribeEvent
    public static void load(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("endinglib:xty")
                        .requires(stack -> stack.hasPermission(CommandConfig.COMMAND_PERMISSION.get()))
                        .then(LimbsCommand.register())
                        .then(GameInvulCommand.register())
                        .then(GameInvul2Command.register())
                        .then(PartialTeleportCommand.register())
                        .then(SpinAttackCommand.register())
                        .then(NoPhysicsCommand.register())
                        .then(Game2Command.register())
                        .then(Map2Command.register(event.getDispatcher()))
                        //.then(GameGunEvolutionCommand.register())
                        .then(GameGhostModeCommand.register())
                        .then(DisableInteractBlockCommand.register())
                        .then(FpsCommand.register())
                        .then(WeaponWarehouseCommand.register())
        );
    }
}
