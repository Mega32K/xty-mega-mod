package com.mega.xty.common.network;

import java.util.concurrent.atomic.AtomicInteger;

public class PacketHandler {
    private static final AtomicInteger id = new AtomicInteger(0);
    private static int id() {
        return id.getAndIncrement();
    }
    public PacketHandler() {

    }
}
