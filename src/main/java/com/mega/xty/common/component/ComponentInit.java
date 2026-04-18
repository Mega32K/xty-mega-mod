package com.mega.xty.common.component;

import com.mega.endinglib.api.data.TagEnum;
import com.mega.endinglib.api.item.component.ComponentTypeBuilder;
import com.mega.endinglib.api.item.component.DataComponents;
import com.mega.endinglib.api.item.component.ItemComponentType;
import com.mega.endinglib.api.item.component.type.EquippableComponent;
import net.minecraft.resources.ResourceLocation;

public class ComponentInit {
    public static final ResourceLocation GUN_FIRE_COMPONENT = new ResourceLocation("tacz", "function/gun_fire");
    public static final ItemComponentType<GunFireComponent> GUN_SHOOT = DataComponents.register(GUN_FIRE_COMPONENT, ComponentTypeBuilder.create(
            builder -> builder
                    .codec(GunFireComponent.CODEC)
                    .registryName(GUN_FIRE_COMPONENT)
                    .rootTagType(TagEnum.SNBT)
                    .build()
    ));
    public static void init() {}
}
