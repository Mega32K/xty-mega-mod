package com.mega.xty.mixin.endinglib;

import com.mega.endinglib.util.mixin.level.ServerExpandedContext;
import com.mega.xty.common.data.map1.Game2SavedData;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.map2.Game1SavedData;
import com.mega.xty.common.data.map2.Map2SavedData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.fps.S2CPlayerKADPacket;
import com.mega.xty.common.network.s2c.fps.S2CPlayerNamePacket;
import com.mega.xty.util.data_expand.SavedDataGetter;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerExpandedContext.class)
public abstract class ServerExpandedContextMixin implements SavedDataGetter {
    @Shadow @Final public MinecraftServer server;
    @Unique
    private Game2SavedData map1game2SavedData = null;
    @Unique
    private Game1SavedData map2game1SavedData = null;
    @Unique
    private com.mega.xty.common.data.map2.Game2SavedData map2game2SavedData = null;
    @Unique
    private FpsSavedData fpsSavedData = null;
    @Unique
    private Map2SavedData map2SavedData = null;

    public Game2SavedData getMap1game2SavedData() {
        if (map1game2SavedData == null)
            map1game2SavedData = Game2SavedData.readOrCreate(this.server);
        return map1game2SavedData;
    }

    public Game1SavedData getMap2game1SavedData() {
        if (map2game1SavedData == null)
            map2game1SavedData = Game1SavedData.readOrCreate(this.server);
        return map2game1SavedData;
    }

    public com.mega.xty.common.data.map2.Game2SavedData getMap2game2SavedData() {
        if (map2game2SavedData == null)
            map2game2SavedData = com.mega.xty.common.data.map2.Game2SavedData.readOrCreate(this.server);
        return map2game2SavedData;
    }

    public FpsSavedData getFpsSavedData() {
        if (fpsSavedData == null)
            fpsSavedData = FpsSavedData.readOrCreate(this.server);
        return fpsSavedData;
    }

    @Override
    public Map2SavedData getMap2SavedData() {
        if (map2SavedData == null)
            map2SavedData = Map2SavedData.readOrCreate(this.server);
        return map2SavedData;
    }

    @Inject(method = "update", at = @At(value = "FIELD", target = "Lcom/mega/endinglib/util/mixin/level/ServerExpandedContext;endingLibrarySavedData:Lcom/mega/endinglib/common/data/EndingLibrarySavedData;"), remap = false)
    private void tick(CallbackInfo ci) {
        if (fpsSavedData != null) {
            if (fpsSavedData.isEnableKAD() && fpsSavedData.isKadDirty())
                NetworkHandler.sendToAll(new S2CPlayerKADPacket(false, fpsSavedData.packDirtyKAD()));
            if (fpsSavedData.isPlayerNamesDirty())
                NetworkHandler.sendToAll(new S2CPlayerNamePacket(false, fpsSavedData.packDirtyTabs(), fpsSavedData.packRemovedPlayerTabs()));
        }
    }
}
