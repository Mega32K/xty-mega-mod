package com.mega.xty.common.data.fps;

import com.mega.endinglib.api.data.CompoundTagReader;
import com.mega.endinglib.api.data.CompoundTagWriter;
import com.mega.xty.common.data.fps.kad.KAD;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public class PlayerNameData {
    public static final CompoundTagReader<PlayerNameData> NBT_READER = (nbt, key) -> {
        MutableComponent m = Component.Serializer.fromJson(nbt.getString(key));
        return m == null ? null : new PlayerNameData(m);
    };
    public static final CompoundTagWriter<PlayerNameData> NBT_WRITER = (nbt, key, name) -> nbt.putString(key, Component.Serializer.toJson(name.component));
    public static FriendlyByteBuf.Reader<PlayerNameData> F_READER = friendlyByteBuf -> new PlayerNameData(friendlyByteBuf.readComponent());
    public static FriendlyByteBuf.Writer<PlayerNameData> F_WRITER = ((friendlyByteBuf, name) -> friendlyByteBuf.writeComponent(name.component));

    public Component component;
    public boolean isDirty;

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

    public PlayerNameData(Component component) {
        this.component = component;
    }
}
