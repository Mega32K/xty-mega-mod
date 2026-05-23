package com.mega.map.common.particle;

import com.mega.map.common.init.ParticleInit;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class Game2HitParticleOption implements ParticleOptions {
    public static final ParticleOptions.Deserializer<Game2HitParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<Game2HitParticleOption>() {
        public @NotNull Game2HitParticleOption fromCommand(@NotNull ParticleType<Game2HitParticleOption> p_123689_, @NotNull StringReader p_123690_) throws CommandSyntaxException {
            p_123690_.expect(' ');
            float f = p_123690_.readFloat();
            return new Game2HitParticleOption(f);
        }

        public @NotNull Game2HitParticleOption fromNetwork(@NotNull ParticleType<Game2HitParticleOption> p_123692_, @NotNull FriendlyByteBuf p_123693_) {
            return new Game2HitParticleOption(p_123693_.readFloat());
        }
    };
    public float zRoll;

    public Game2HitParticleOption(float zRoll) {
        this.zRoll = zRoll;
    }

    public float getzRoll() {
        return zRoll;
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return ParticleInit.GAME2_HIT.get();
    }

    @Override
    public void writeToNetwork(@NotNull FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(zRoll);
    }
    @Override
    public @NotNull String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), zRoll);
    }
}
