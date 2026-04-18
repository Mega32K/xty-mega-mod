package com.mega.xty.common.data.map2;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.game1.*;
import com.mega.xty.util.data_expand.SavedDataGetter;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Game1SavedData extends SavedData {
    public MinecraftServer server;
    private boolean isStopped = true;
    private Game1Functions game1Functions = new Game1Functions(this);
    private final ObjectList<ObjectList<ItemStack>> evolutionWeapons = Util.make(() -> {
        ObjectList<ObjectList<ItemStack>> defaultMap = new ObjectArrayList<>();
        for (int i=0;i<13;i++) {
            defaultMap.add(new ObjectArrayList<>());
        }
        return defaultMap;
    });
    private final Object2ObjectOpenHashMap<UUID, ByteList> playerRandomSelector = new Object2ObjectOpenHashMap<>();
    public static Game1SavedData readOrCreate(MinecraftServer server) {
        Game1SavedData data = server.overworld().getDataStorage().computeIfAbsent(tag-> load(tag,server), Game1SavedData::new, "xty_map2_game_1");
        data.server = server;
        return data;
    }

    public static Game1SavedData getInstance(MinecraftServer server) {
        return ((SavedDataGetter) ((ServerEC) server).endinglib$serverECData()).getMap2game1SavedData();
    }
    public static Game1SavedData load(CompoundTag tag, MinecraftServer server) {
        Game1SavedData data = new Game1SavedData();
        if (CompoundTagUtils.containsBoolean(tag, "Stopped"))
            data.isStopped = tag.getBoolean("Stopped");
        if (CompoundTagUtils.containsCompound(tag, "Game1Functions"))
            data.game1Functions = Game1Functions.load(tag.getCompound("Game1Functions"), server, data);
        if (CompoundTagUtils.containsListTag(tag, "EvolutionWeapons")) {
            ListTag weaponsMap = tag.getList("EvolutionWeapons", Tag.TAG_LIST);
            for (int i=0;i<weaponsMap.size();i++) {
                ListTag weaponsListTag = weaponsMap.getList(i);
                ObjectList<ItemStack> weapons = data.evolutionWeapons.get(i);
                if (!weaponsListTag.isEmpty()) {
                    for (int j=0;j<weaponsListTag.size();j++) {
                        ItemStack itemStack = ItemStack.of(weaponsListTag.getCompound(j));
                        weapons.add(itemStack);
                    }
                }
            }
        }
        if (CompoundTagUtils.containsCompound(tag, "RandomSelectorData")) {
            CompoundTag randomSelectorData = tag.getCompound("RandomSelectorData");
            for (String uuidKey : randomSelectorData.getAllKeys()) {
                ByteList selector = new ByteArrayList(randomSelectorData.getByteArray(uuidKey));
                data.playerRandomSelector.put(UUID.fromString(uuidKey), selector);
            }
        }
        return data;
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putBoolean("Stopped", this.isStopped);
        CompoundTag functions = new CompoundTag();
        if (this.game1Functions != null) this.game1Functions.save(functions);
        tag.put("Game1Functions", functions);
        if (!this.evolutionWeapons.isEmpty()) {
            ListTag weaponsMap = new ListTag();
            for (ObjectList<ItemStack> weapons : this.evolutionWeapons) {
                ListTag weaponsListTag = new ListTag();
                for (ItemStack itemStack : weapons) {
                    weaponsListTag.add(itemStack.save(new CompoundTag()));
                }
                weaponsMap.add(weaponsListTag);
            }
            tag.put("EvolutionWeapons", weaponsMap);
        }
        if (!this.playerRandomSelector.isEmpty()) {
            CompoundTag randomSelectorData = new CompoundTag();
            for (var entry : this.playerRandomSelector.object2ObjectEntrySet()) {
                randomSelectorData.putByteArray(entry.getKey().toString(), entry.getValue());
            }
            tag.put("RandomSelectorData", randomSelectorData);
        }
        return tag;
    }

    public void setStopped(boolean stopped) {
        if (this.isStopped != stopped) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CGame1StatsPacket(stopped), serverPlayer);
        }
        isStopped = stopped;
    }
    public boolean isStopped() {
        return isStopped;
    }
    public Game1Functions getGame1Functions() {
        return game1Functions;
    }
    public List<ItemStack> getWeaponsFrom(int index) {
        return ObjectLists.unmodifiable(this.evolutionWeapons.get(index));
    }
    public void putEvolutionWeapons(int index, List<ItemStack> itemStacks) {
        if (index < 0 || index > 12)
            throw new IndexOutOfBoundsException("index of evolutions is out of range [0, 12], current index is " + index);
        if (itemStacks == null || itemStacks.isEmpty())
            this.evolutionWeapons.set(index, new ObjectArrayList<>());
        else this.evolutionWeapons.set(index, new ObjectArrayList<>(itemStacks));
        this.setDirty();
    }
    public ByteList getOrPutPlayerSelector(UUID uuid) {
        if (!this.playerRandomSelector.containsKey(uuid)) {
            this.playerRandomSelector.put(uuid, new ByteArrayList());
        }
        return ByteLists.unmodifiable(this.playerRandomSelector.get(uuid));
    }
    public void spawnPlayerSelector(ServerPlayer player) {
        UUID uuid = player.getUUID();
        RandomSource rand = player.getRandom();
        ByteList selector = new ByteArrayList();
        for (ObjectList<ItemStack> weapons : this.evolutionWeapons) {
            if (weapons.size() <= 1)
                selector.add((byte) 0);
            else {
                selector.add((byte) (rand.nextInt(0, weapons.size())));
            }
        }
        this.playerRandomSelector.put(uuid, selector);
        NetworkHandler.sendToPlayer(new S2CGame1EvolutionSelectorPacket(selector.toByteArray()), player);
        NetworkHandler.sendToPlayer(new S2CGame1EvolutionWeaponPacket(this.getSelectedWeapons(player)), player);
        this.setDirty();
    }
    public List<ItemStack> getSelectedWeapons(Player player) {
        if (this.playerRandomSelector.containsKey(player.getUUID())){
            ByteList selector = this.playerRandomSelector.get(player.getUUID());
            if (this.evolutionWeapons.size() == 13) {
                List<ItemStack> list = new ObjectArrayList<>();
                for (int i=0;i<this.evolutionWeapons.size();i++) {
                    List<ItemStack> toSelect = this.evolutionWeapons.get(i);
                    int index = 0;
                    try {
                        if (!selector.isEmpty())
                            index = selector.getByte(i);
                    } catch (IndexOutOfBoundsException throwable) {
                        throwable.printStackTrace();
                    }
                    if (!toSelect.isEmpty()) list.add(toSelect.get(index));
                }
                return list;
            }
        }
        return List.of();
    }
}
