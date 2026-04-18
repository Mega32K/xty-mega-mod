package com.mega.xty.common.init;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.particle.Game2HitParticleOption;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ParticleInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, XtyMegaMod.MODID);
    public static final RegistryObject<ParticleType<Game2HitParticleOption>> GAME2_HIT = PARTICLE_TYPES.register("game2_hit", ()-> new ParticleType<>(false, Game2HitParticleOption.DESERIALIZER) {
        @Override
        public @NotNull Codec<Game2HitParticleOption> codec() {
            return Codec.FLOAT.xmap(Game2HitParticleOption::new, Game2HitParticleOption::getzRoll);
        }
    });
}
