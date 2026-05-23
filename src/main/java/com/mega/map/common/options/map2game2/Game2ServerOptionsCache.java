package com.mega.map.common.options.map2game2;

public final class Game2ServerOptionsCache {
    public static final Game2ServerOptions CURRENT = new Game2ServerOptions();

    private Game2ServerOptionsCache() {
    }

    public static void updateFrom(Game2ServerOptions source) {
        CURRENT.copyFrom(source);
    }
}
