package com.mega.map.common.capability;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public enum Limbs {
    LEFT_HAND((byte) 1, "leftHand"),
    RIGHT_HAND((byte) 2, "rightHand"),
    LEFT_LEG((byte) 4, "leftLeg"),
    RIGHT_LEG((byte) 8, "rightLeg"),
    BODY((byte) 16, "body");
    public static final Map<String, Limbs> MAP = new Object2ObjectOpenHashMap<>(5);
    private final byte flag;
    private final String name;
    Limbs(byte flag, String name) {
        this.flag = flag;
        this.name = name;
    }
    static {
        for (Limbs limbs : Limbs.values())
            MAP.put(limbs.getName(), limbs);
    }
    @Nullable
    public static Limbs fromName(String name) {
        return MAP.get(name);
    }

    public String getName() {
        return name;
    }

    public byte getFlag() {
        return flag;
    }
}
