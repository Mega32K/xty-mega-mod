package com.mega.xty.common.data.fps;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum DeathSourceType {
    DEFAULT,
    HEADSHOT,
    BURN,
    BLAST,
    SLASH,
    SHOCK;
    public final String name = this.name().toLowerCase();
    public MutableComponent toFontContext() {
        return Component.translatable("fps.death_type." + this.name);
    }
}
