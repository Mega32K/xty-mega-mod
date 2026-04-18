package com.mega.xty.common.component;

import com.mega.endinglib.api.item.component.type.function.FunctionComponent;
import com.mega.endinglib.api.item.component.type.function.UseEventComponent;
import com.mega.endinglib.mixin.accessor.AccessorCommandSourceStack;
import com.mega.endinglib.util.mc.codec.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public record GunFireComponent (String command, Optional<ResourceLocation> function, int cooldownTicks, int minimumPermission, boolean silent, boolean cancelShoot) implements FunctionComponent {
    public static Codec<GunFireComponent> CODEC = RecordCodecBuilder.create(
            com -> com.group(
                    Codec.STRING.optionalFieldOf("command","").forGetter(GunFireComponent::command),
                    ResourceLocation.CODEC.optionalFieldOf("function").forGetter(GunFireComponent::function),
                    Codec.INT.optionalFieldOf("cooldown_ticks", 0).forGetter(GunFireComponent::cooldownTicks),
                    Codecs.NON_NEGATIVE_INT.optionalFieldOf("min_permission", 2).forGetter(GunFireComponent::minimumPermission),
                    Codec.BOOL.optionalFieldOf("silent", true).forGetter(GunFireComponent::silent),
                    Codec.BOOL.optionalFieldOf("cancel_shoot", false).forGetter(GunFireComponent::cancelShoot)
            ).apply(com, GunFireComponent::new)
    );

    public void apply(ServerLevel serverLevel, LivingEntity user, InteractionHand hand) {
        if (!command.isEmpty()) {
            CommandSourceStack sourceStack = user.createCommandSourceStack().withMaximumPermission(minimumPermission);
            if (silent)
                sourceStack = sourceStack.withSuppressedOutput();
            serverLevel.getServer().getCommands().performPrefixedCommand(sourceStack, this.command);
        }
        function.ifPresent(location -> this.apply(user, location, minimumPermission));
        if (user instanceof Player player) {
            if (cooldownTicks != 0)
                player.getCooldowns().addCooldown(user.getItemInHand(hand).getItem(), cooldownTicks);
        }
    }
}
