package com.mega.map.common.component;

import com.mega.endinglib.api.item.component.type.function.FunctionComponent;
import com.mega.endinglib.util.mc.codec.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public record ItemSwitchComponent(ResourceLocation function, int minimumPermission) implements FunctionComponent {
    public static final Codec<ItemSwitchComponent> CODEC = RecordCodecBuilder.create(
            com -> com.group(
                    ResourceLocation.CODEC.fieldOf("function").forGetter(ItemSwitchComponent::function),
                    Codecs.NON_NEGATIVE_INT.optionalFieldOf("min_permission", 2).forGetter(ItemSwitchComponent::minimumPermission)
            ).apply(com, ItemSwitchComponent::new)
    );

    public void apply(ServerPlayer player, InteractionHand hand) {
        this.apply(player, this.function, this.minimumPermission);
    }
}
