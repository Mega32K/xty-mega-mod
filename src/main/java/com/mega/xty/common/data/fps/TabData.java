package com.mega.xty.common.data.fps;

import com.mega.endinglib.api.data.CompoundTagReader;
import com.mega.endinglib.api.data.CompoundTagWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;

public class TabData {
    public static final CompoundTagReader<TabData> NBT_READER = (nbt, key) -> {
        CompoundTag tag = nbt.getCompound(key);
        MutableComponent m = Component.Serializer.fromJson(tag.getString("Component"));
        return m == null ? null : new TabData(tag.getBoolean("Dead"), m);
    };
    public static final CompoundTagWriter<TabData> NBT_WRITER = (nbt, key, name) -> {
        CompoundTag tag = new CompoundTag();
        if (name.isDead)
            tag.putBoolean("Dead", true);
        tag.putString("Component", Component.Serializer.toJson(name.component));
        nbt.put(key, tag);
    };
    public static FriendlyByteBuf.Reader<TabData> F_READER = friendlyByteBuf -> new TabData(friendlyByteBuf.readBoolean(), friendlyByteBuf.readComponent());
    public static FriendlyByteBuf.Writer<TabData> F_WRITER = ((friendlyByteBuf, name) -> {
        friendlyByteBuf.writeBoolean(name.isDead);
        friendlyByteBuf.writeComponent(name.component);
    });

    public Component component;
    public boolean isDead;
    public boolean isDirty;

    public TabData(boolean isDead, Component component) {
        this.isDead = isDead;
        this.component = component;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public void setComponent(Component component) {
        if (!Objects.equals(component, this.component)) {
            this.component = component;
            this.setDirty(true);
        }
    }
    public boolean isDead() {
        return isDead;
    }
    public void setDead(boolean dead) {
        if (this.isDead != dead) {
            isDead = dead;
            this.setDirty(true);
        }
    }
}
