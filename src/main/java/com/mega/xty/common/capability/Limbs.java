package com.mega.xty.common.capability;

public enum Limbs {
    LEFT_HAND((byte) 1),
    RIGHT_HAND((byte) 2),
    LEFT_LEG((byte) 4),
    RIGHT_LEG((byte) 8),
    BODY((byte) 16);

    private final byte flag;
    Limbs(byte flag) {
        this.flag = flag;
    }

    public byte getFlag() {
        return flag;
    }
}
