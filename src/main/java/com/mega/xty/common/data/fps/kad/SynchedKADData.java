package com.mega.xty.common.data.fps.kad;

import com.mega.endinglib.api.data.CompoundTagReader;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.api.data.CompoundTagWriter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class SynchedKADData {
    public static final SynchedKADData EMPTY_KAD = new SynchedKADData(Map.of(
                    KAD.KAD_GENERAL, new KAD(0, 0, 0),
                    KAD.KAD_CURRENT, new KAD(0, 0, 0)
            )) {
        @Override
        public boolean setKAD(String key, KAD kad) {
            return false;
        }
        @Override
        public boolean modifyKAD(String key, KAD newKad) {
            return false;
        }
    };
    public static final CompoundTagReader<SynchedKADData> NBT_READER = (nbt, key) -> new SynchedKADData(CompoundTagUtils.getMap(nbt, key, CompoundTag::getString, KAD.NBT_READER));
    public static final CompoundTagWriter<SynchedKADData> NBT_WRITER = (nbt, key, data) -> CompoundTagUtils.putMap(nbt, key, data.getData(), CompoundTag::putString, KAD.NBT_WRITER);
    public static final FriendlyByteBuf.Reader<SynchedKADData> F_READER = friendlyByteBuf -> new SynchedKADData(friendlyByteBuf.readMap(FriendlyByteBuf::readUtf, KAD.F_READER));
    public static final FriendlyByteBuf.Writer<SynchedKADData> F_WRITER = (friendlyByteBuf, data) -> friendlyByteBuf.writeMap(data.data, FriendlyByteBuf::writeUtf, KAD.F_WRITER);
    @NotNull
    public final Map<String, KAD> data;
    protected boolean isDirty;
    public SynchedKADData(Map<String, KAD> data) {
        this.data = new Object2ObjectOpenHashMap<>(data);
    }
    public boolean isDirty() {
        return isDirty;
    }
    public void setDirty() {
        this.setDirty(true);
    }
    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public Map<String, KAD> getData() {
        return Collections.unmodifiableMap(data);
    }

    public KAD getOrDefaultKAD(String key) {
        return data.getOrDefault(key, new KAD(0, 0, 0));
    }
    @Nullable
    public KAD getKAD(String key) {
        return data.get(key);
    }

    public void ifPresent(String key, Consumer<KAD> consumer) {
        Optional.ofNullable(data.get(key)).ifPresent(consumer);
    }

    /**
     * @param key KAD名
     * @param kad KAD数据
     * @return true表示值发生了变化
     */
    public boolean setKAD(String key, KAD kad) {
        KAD prev = data.get(key);
        boolean changed = false;
        if (!Objects.equals(prev, kad)) {
            data.put(key, kad);
            setDirty();
            changed = true;
        }
        return changed;
    }
    public boolean modifyKAD(String key, KAD newKad) {
        KAD prev = data.get(key);
        boolean changed = false;
        if (!Objects.equals(prev, newKad)) {
            if (prev == null) {
                data.put(key, newKad);
            } else {
                prev.kills = newKad.kills;
                prev.assists = newKad.assists;
                prev.deaths = newKad.deaths;
            }
            changed = true;
            setDirty();
        }
        return changed;
    }
    public void modifyKAD(String key, Consumer<KAD> modifier) {
        KAD prev = data.get(key);
        if (prev == null) {
            prev = new KAD(0, 0, 0);
            modifier.accept(prev);
            data.put(key, prev);
        } else {
            modifier.accept(prev);
        }
        setDirty();
    }
}
