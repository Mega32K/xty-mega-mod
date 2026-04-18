package com.mega.xty.common.data.fps.kad;

import com.mega.endinglib.api.data.CompoundTagReader;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.api.data.CompoundTagWriter;
import com.mega.xty.common.data.fps.FpsSavedData;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServerSynchedKADData extends SynchedKADData {
    public static final Function<FpsSavedData, ServerSynchedKADData> EMPTY_KAD_CREATOR = savedData ->  {
        return new ServerSynchedKADData(Map.of(
                KAD.KAD_GENERAL, new KAD(0, 0, 0),
                KAD.KAD_CURRENT, new KAD(0, 0, 0)
        ), savedData) {
            @Override
            public boolean setKAD(String key, KAD kad) {
                return false;
            }

            @Override
            public boolean modifyKAD(String key, KAD newKad) {
                return false;
            }
        };
    };
    public static final Function<FpsSavedData, CompoundTagReader<ServerSynchedKADData>> NBT_READER = savedData -> {
        return (nbt, key) -> new ServerSynchedKADData(SynchedKADData.NBT_READER.apply(nbt, key).data, savedData);
    };
    public static final CompoundTagWriter<ServerSynchedKADData> NBT_WRITER = (nbt, key, data) -> CompoundTagUtils.putMap(nbt, key, data.getData(), CompoundTag::putString, KAD.NBT_WRITER);
    public final FpsSavedData savedData;
    public ServerSynchedKADData(Map<String, KAD> data, FpsSavedData savedData) {
        super(data);
        this.savedData = savedData;
    }

    @Override
    public boolean setKAD(String key, KAD kad) {
        boolean z = super.setKAD(key, kad);
        if (z) {
            savedData.setDirty();
            savedData.setKadDirty(true);
        }
        return z;
    }

    @Override
    public boolean modifyKAD(String key, KAD newKad) {
        boolean z = super.modifyKAD(key, newKad);
        if (z) {
            savedData.setDirty();
            savedData.setKadDirty(true);
        }
        return z;
    }

    @Override
    public void modifyKAD(String key, Consumer<KAD> modifier) {
        KAD lastKAD = this.getKAD(key);
        super.modifyKAD(key, modifier);
        if (!Objects.equals(lastKAD, this.getKAD(key))) {
            savedData.setDirty();
            savedData.setKadDirty(true);
        }
    }

    public static ServerSynchedKADData createDefaultKAD(FpsSavedData savedData) {
        return new ServerSynchedKADData(Map.of(
                KAD.KAD_GENERAL, new KAD(0, 0, 0),
                KAD.KAD_CURRENT, new KAD(0, 0, 0)
        ), savedData);
    }
}
