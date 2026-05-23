package com.mega.map.common.init;

import com.mega.map.MegaMod;
import com.mega.map.common.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MegaMod.MODID);
    public static final RegistryObject<EntityType<WineBottleEntity>> BINDING = ENTITIES.register("wine", ()->
            EntityType.Builder.of(WineBottleEntity::new, MobCategory.MISC)
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(10)
                    .build("wine")
    );
    public static final RegistryObject<EntityType<ShadowPlayerEntity>> SHADOW_PLAYER = ENTITIES.register("shadow_player", ()->
            EntityType.Builder.<ShadowPlayerEntity>of(ShadowPlayerEntity::new, MobCategory.MISC)
                    .sized(0, 0).noSave().noSummon().clientTrackingRange(6).updateInterval(20)
                    .build("shadow_player")
    );
    public static final RegistryObject<EntityType<BlackHoleEntity>> BLACKHOLE = ENTITIES.register("black_hole", ()->
            EntityType.Builder.<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(20)
                    .build("black_hole")
    );
    public static final RegistryObject<EntityType<ThrownItemEntity>> THROWN_ITEM = ENTITIES.register("thrown_item", ()->
            EntityType.Builder.of(ThrownItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(20)
                    .build("thrown_item")
    );
    public static final RegistryObject<EntityType<Game2ItemEntity>> GAME_ITEM = ENTITIES.register("game2_item", ()->
            EntityType.Builder.<Game2ItemEntity>of(Game2ItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(16).updateInterval(20)
                    .build("game2_item")
    );
    public static final RegistryObject<EntityType<C4Entity>> C4 = ENTITIES.register("c4", ()->
            EntityType.Builder.<C4Entity>of(C4Entity::new, MobCategory.MISC)
                    .sized(0.5F, 0.25F).clientTrackingRange(16).updateInterval(20)
                    .build("c4")
    );
}
