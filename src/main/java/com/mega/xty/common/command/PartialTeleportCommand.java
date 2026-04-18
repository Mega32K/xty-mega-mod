package com.mega.xty.common.command;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.common.command.argument.EasingArgument;
import com.mega.endinglib.common.network.PacketHandler;
import com.mega.endinglib.util.mixin.data_expand.ExtraEntity;
import com.mega.xty.common.command.argument.LimbArgumentType;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.S2CPartialTeleportPacket;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class PartialTeleportCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("partialTeleport")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("duration", IntegerArgumentType.integer(0))
                                .then(Commands.argument("targetPos", Vec3Argument.vec3(true))
                                        .then(Commands.argument("easing", EasingArgument.easing())
                                                .executes(context -> partialTeleport(context.getSource(), EntityArgument.getPlayer(context, "target"), EasingArgument.getEasing(context, "easing"), IntegerArgumentType.getInteger(context, "duration"), Vec3Argument.getCoordinates(context, "targetPos")))
                                        )
                                )
                        )
                );
    }
    private static int partialTeleport(CommandSourceStack sourceStack, ServerPlayer player, Easing easing, int duration, Coordinates coordinates) {
        Vec3 vec3 = coordinates.getPosition(sourceStack);
        CommonProxy.getXtyCap(player).ifPresent(capability -> {
            capability.smoothStartPos = new Vec3(player.xOld, player.yOld, player.zOld);
            capability.smoothTeleportTarget = vec3;
            capability.smoothTeleportDuration = duration;
            capability.smoothTeleportStart = ExtraEntity.of(player).endinglib$getExtraEntityData().tickCount;
            capability.smoothTeleportEasing = easing;
            capability.canUsePartialTeleportAnim = true;
        });
        for (ServerPlayer sendTo : player.serverLevel().players()) {
            NetworkHandler.sendToPlayer(new S2CPartialTeleportPacket(player.getUUID(), easing, duration, vec3.toVector3f()), sendTo);
        }
        return 0;
    }
}
