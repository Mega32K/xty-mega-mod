package com.mega.xty.common.capability;

import com.mega.endinglib.api.capability.CapabilityEntityData;
import com.mega.endinglib.api.capability.CapabilitySyncType;
import com.mega.endinglib.api.capability.EntitySyncCapabilityBase;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.data.fps.DeathSourceType;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.S2CAddDeathDataPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class FpsCapability extends EntitySyncCapabilityBase {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "fps");
    @Nullable
    private UUID assisterID;
    @Nullable
    private Player assister;
    private float assisterDamage;
    @Nullable
    private UUID assisterID2;
    @Nullable
    private Player assister2;
    private float assisterDamage2;
    public final Map<UUID, DeathMessage> deathMessageMap = new Object2ObjectOpenHashMap<>();
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }
    @Override
    protected @NotNull Predicate<Entity> canAttach() {
        return entity -> entity instanceof Player;
    }
    @Override
    public void onSyncedDataUpdated(CapabilityEntityData<?> data) {
    }

    @Override
    public void syncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {
    }
    @Override
    public void readSyncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {

    }

    @Override
    public boolean canSyncWhenTick(Entity entity, Level level) {
        return false;
    }

    @Override
    public void customSerializeNBT(CompoundTag compoundTag) {
        if (this.assisterDamage > 0.0F)
            compoundTag.putFloat("assisterDamage", this.assisterDamage);
        if (this.assisterID != null)
            compoundTag.putUUID("assister", this.assisterID);
        if (this.assisterDamage2 > 0.0F)
            compoundTag.putFloat("assisterDamage2", this.assisterDamage2);
        if (this.assisterID2 != null)
            compoundTag.putUUID("assister2", this.assisterID2);
    }

    @Override
    public void customDeserializeNBT(CompoundTag compoundTag) {
        if (CompoundTagUtils.containsFloat(compoundTag, "assisterDamage"))
            this.assisterDamage = compoundTag.getFloat("assisterDamage");
        if (CompoundTagUtils.containsFloat(compoundTag, "assisterDamage2"))
            this.assisterDamage2 = compoundTag.getFloat("assisterDamage2");
        if (compoundTag.hasUUID("assister")) {
            this.assisterID = compoundTag.getUUID("assister");
            if (this.getEntity() instanceof Player player)
                this.assister =  player.level().getPlayerByUUID(this.assisterID);
        }
        if (compoundTag.hasUUID("assister2")) {
            this.assisterID2 = compoundTag.getUUID("assister2");
            if (this.getEntity() instanceof Player player)
                this.assister2 =  player.level().getPlayerByUUID(this.assisterID2);
        }
    }

    @Override
    protected void tick(Entity entity) {
        if (entity instanceof Player player) {
            if (!this.deathMessageMap.isEmpty()) {
                synchronized (deathMessageMap) {
                    if (!this.deathMessageMap.isEmpty()) {
                        for (var message : deathMessageMap.values()) {
                            if (message.killedMessageKiller != null && !message.killedMessageTypes.isEmpty() && message.killedMessageKilled != null) {
                                NetworkHandler.sendToAll(new S2CAddDeathDataPacket(message.killedMessageKiller, message.killedWeapon, message.makeDeathTypeComponent().append(message.killedMessageKilled)));
                            }
                        }
                        deathMessageMap.clear();
                    }
                }
            }
            if (player instanceof ServerPlayer sp && sp.tickCount % 20 == 0) {
                FpsSavedData fpsSavedData = FpsSavedData.getInstance(sp.server);
                fpsSavedData.setPlayerName(sp);
            }
        }
    }

    public DeathMessage getOrDefaultDeathMessage(Entity entity) {
        DeathMessage deathMessage = deathMessageMap.get(entity.getUUID());
        if (deathMessage == null) {
            deathMessage = new DeathMessage();
            deathMessageMap.put(entity.getUUID(), deathMessage);
        }
        return deathMessage;
    }

    @Nullable
    public Player checkAndGetAssister(Level level) {
        if (this.assister == null && this.assisterID != null)
            this.assister = level.getPlayerByUUID(this.assisterID);
        return assister;
    }
    public void setAssister(@Nullable Player assister) {
        if (assister == null) {
            this.assister = null;
            this.assisterID = null;
        } else {
            this.assister = assister;
            this.assisterID = assister.getUUID();
        }
    }

    @Nullable
    public Player checkAndGetAssister2(Level level) {
        if (this.assister2 == null && this.assisterID2 != null)
            this.assister2 = level.getPlayerByUUID(this.assisterID2);
        return assister2;
    }
    public void setAssister2(@Nullable Player assister) {
        if (assister == null) {
            this.assister2 = null;
            this.assisterID2 = null;
        } else {
            this.assister2 = assister;
            this.assisterID2 = assister.getUUID();
        }
    }
    public float getAssisterDamage() {
        return assisterDamage;
    }

    public void setAssisterDamage(float assisterDamage) {
        this.assisterDamage = assisterDamage;
    }
    public float getAssisterDamage2() {
        return assisterDamage2;
    }

    public void setAssisterDamage2(float assisterDamage) {
        this.assisterDamage2 = assisterDamage;
    }
    public static class DeathMessage {
        private MutableComponent killedMessageKiller;
        private final Set<DeathSourceType> killedMessageTypes = new ObjectOpenHashSet<>();
        private Component killedMessageKilled;
        @NotNull
        private ItemStack killedWeapon = ItemStack.EMPTY;
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
}
