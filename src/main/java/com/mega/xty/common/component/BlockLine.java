package com.mega.xty.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Objects;

public record BlockLine(BlockPos start, BlockPos end) {
    public static final Codec<BlockLine> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockPos.CODEC.fieldOf("start").forGetter(BlockLine::start),
                    BlockPos.CODEC.fieldOf("end").forGetter(BlockLine::end)
            ).apply(instance, BlockLine::new)
    );

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockLine blockLine = (BlockLine) o;
        return Objects.equals(start, blockLine.start) && Objects.equals(end, blockLine.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
