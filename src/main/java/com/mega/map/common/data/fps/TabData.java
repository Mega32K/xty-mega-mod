package com.mega.map.common.data.fps;

import com.mega.endinglib.api.data.CompoundTagReader;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.api.data.CompoundTagWriter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;
import java.util.Optional;

public class TabData {
    public static final CompoundTagReader<TabData> NBT_READER = (nbt, key) -> {
        CompoundTag tag = nbt.getCompound(key);
        MutableComponent m = Component.Serializer.fromJson(tag.getString("Component"));
        return m == null ? null : new TabData(tag.getBoolean("Dead"), m, Optional.ofNullable((ChatFormatting)CompoundTagUtils.getEnum(tag, "Color")).orElse(ChatFormatting.WHITE));
    };
    public static final CompoundTagWriter<TabData> NBT_WRITER = (nbt, key, name) -> {
        CompoundTag tag = new CompoundTag();
        if (name.isDead)
            tag.putBoolean("Dead", true);
        tag.putString("Component", Component.Serializer.toJson(name.component));
        if (name.teamColor != null)
            CompoundTagUtils.putEnum(tag, "Color", name.teamColor);
        nbt.put(key, tag);
    };
    public static FriendlyByteBuf.Reader<TabData> F_READER = friendlyByteBuf -> new TabData(friendlyByteBuf.readBoolean(), friendlyByteBuf.readComponent(), friendlyByteBuf.readEnum(ChatFormatting.class));
    public static FriendlyByteBuf.Writer<TabData> F_WRITER = ((friendlyByteBuf, name) -> {
        friendlyByteBuf.writeBoolean(name.isDead);
        friendlyByteBuf.writeComponent(name.component);
        friendlyByteBuf.writeEnum(name.teamColor == null ? ChatFormatting.WHITE : name.teamColor);
    });

    public Component component;
    public boolean isDead;
    public ChatFormatting teamColor;
    public boolean isDirty;

    public TabData(boolean isDead, Component component, ChatFormatting teamColor) {
        this.isDead = isDead;
        this.component = component;
        this.teamColor = teamColor;
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

    public void setTeamColor(ChatFormatting teamColor) {
        if (!Objects.equals(this.teamColor, teamColor)) {
            this.teamColor = teamColor;
            this.setDirty(true);
        }
    }
}
