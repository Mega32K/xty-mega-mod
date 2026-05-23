package com.mega.map.common.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public interface XtyDamageTypeTags extends DamageTypeTags {
    TagKey<DamageType> GAME_INVULNERABLE_BYPASS = create("megamod:game_invulnerable_bypasses");
    TagKey<DamageType> GAME_INVULNERABLE_BYPASS2 = create("megamod:game_invulnerable_not_bypasses2");
    private static TagKey<DamageType> create(String rl) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(rl));
    }
}
