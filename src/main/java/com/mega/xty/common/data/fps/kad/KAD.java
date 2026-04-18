package com.mega.xty.common.data.fps.kad;

import com.mega.endinglib.api.data.CompoundTagReader;
import com.mega.endinglib.api.data.CompoundTagWriter;
import net.minecraft.network.FriendlyByteBuf;

public class KAD {
    public static final String KAD_GENERAL = "general";
    public static final String KAD_CURRENT = "current";
    public static final CompoundTagReader<KAD> NBT_READER = (nbt, key) -> KAD.deserialize(nbt.getInt(key));
    public static final CompoundTagWriter<KAD> NBT_WRITER = (nbt, key, kad) -> nbt.putInt(key, kad.serialize());
    public static FriendlyByteBuf.Reader<KAD> F_READER = friendlyByteBuf -> KAD.deserialize(friendlyByteBuf.readInt());
    public static FriendlyByteBuf.Writer<KAD> F_WRITER = ((friendlyByteBuf, kad) -> friendlyByteBuf.writeInt(kad.serialize()));
    public int kills;
    public int assists;
    public int deaths;
    KAD(int kills, int assists, int deaths) {
        this.kills = kills;
        this.assists = assists;
        this.deaths = deaths;
    }
    public KAD kills(int kills) {
        this.kills = kills;
        return this;
    }
    public KAD assists(int assists) {
        this.assists = assists;
        return this;
    }
    public KAD deaths(int deaths) {
        this.deaths = deaths;
        return this;
    }
    public static KAD deserialize(int i) {
        return new KAD(i >>> 24, i >> 16 & 0xFF, i >> 8 & 0xFF);
    }
    public int serialize() {
        return kills << 24 | assists << 16 | deaths << 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KAD kad = (KAD) o;
        return kills == kad.kills && assists == kad.assists && deaths == kad.deaths;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + kills;
        result = 31 * result + assists;
        result = 31 * result + deaths;
        return result;
    }
}
