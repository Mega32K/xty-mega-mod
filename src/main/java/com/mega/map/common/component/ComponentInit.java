package com.mega.map.common.component;

import com.mega.map.MegaMod;
import com.mega.endinglib.api.data.TagEnum;
import com.mega.endinglib.api.item.component.ComponentTypeBuilder;
import com.mega.endinglib.api.item.component.DataComponents;
import com.mega.endinglib.api.item.component.ItemComponentType;
import net.minecraft.resources.ResourceLocation;

public class ComponentInit {
    public static final ResourceLocation COM_FILL_CREATOR = ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "debug/fill_creator");
    public static final ResourceLocation GUN_FIRE_COMPONENT = new ResourceLocation("tacz", "function/gun_fire");
    public static final ResourceLocation ITEM_SWITCH_COMPONENT = ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "function/item_switch");
    public static final ItemComponentType<GunFireComponent> GUN_SHOOT = DataComponents.register(GUN_FIRE_COMPONENT, ComponentTypeBuilder.create(
            builder -> builder
                    .codec(GunFireComponent.CODEC)
                    .registryName(GUN_FIRE_COMPONENT)
                    .rootTagType(TagEnum.SNBT)
                    .build()
    ));
    public static final ItemComponentType<ItemSwitchComponent> ITEM_SWITCH = DataComponents.register(ITEM_SWITCH_COMPONENT, ComponentTypeBuilder.create(
            builder -> builder
                    .codec(ItemSwitchComponent.CODEC)
                    .registryName(ITEM_SWITCH_COMPONENT)
                    .rootTagType(TagEnum.SNBT)
                    .build()
    ));
    public static final ItemComponentType<FillCreatorComponent> FILL_CREATOR = DataComponents.register(
            COM_FILL_CREATOR,
            ComponentTypeBuilder.create(builder -> builder
                    .registryName(COM_FILL_CREATOR)
                    .codec(FillCreatorComponent.CODEC)
                    .rootTagType(TagEnum.LIST)
                    .build()
            )
    );

    public static void init() {}
}
