package com.mega.map.common.capability;

import com.mega.map.common.data.fps.DeathSourceType;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class DeathMessage {
    public MutableComponent killedMessageKiller;
    public final Set<DeathSourceType> killedMessageTypes = new ObjectOpenHashSet<>();
    public Component killedMessageKilled;
    @NotNull
    public ItemStack killedWeapon = ItemStack.EMPTY;

    public void putIfAbsentKiller(MutableComponent component) {
        if (this.killedMessageKiller == null)
            this.killedMessageKiller = component;
    }

    public void putIfAbsentKilled(Component component) {
        if (this.killedMessageKilled == null)
            this.killedMessageKilled = component;
    }

    public void makeDeathType(Collection<DeathSourceType> types) {
        this.killedMessageTypes.addAll(types);
        if (this.killedMessageTypes.contains(DeathSourceType.DEFAULT) || this.killedMessageTypes.contains(DeathSourceType.HEADSHOT))
            this.killedMessageTypes.remove(DeathSourceType.SLASH);
    }

    public MutableComponent makeDeathTypeComponent() {
        MutableComponent baseMessage = Component.literal("");
        for (var type : killedMessageTypes)
            baseMessage.append(type.toFontContext());
        return baseMessage;
    }

    public void setKilledWeapon(@NotNull ItemStack killedWeapon) {
        this.killedWeapon = killedWeapon;
    }
}
