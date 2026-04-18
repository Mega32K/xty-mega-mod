package com.mega.xty.common.data.map1;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.S2CDisableBIPacket;
import com.mega.xty.common.network.s2c.map1.game2.S2CGame2StatsPacket;
import com.mega.xty.util.data_expand.SavedDataGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Game2SavedData extends SavedData {
    public MinecraftServer server;
    private boolean isStopped = true;
    private boolean sceneChanging = false;
    private boolean disableBlockInteraction = false;
    private Game2Functions game2Functions = new Game2Functions(this);
    @Nullable
    private String healthScoreObjectiveName = null;
    @Nullable
    private String maxHealthScoreObjectiveName = null;
    public static Game2SavedData readOrCreate(MinecraftServer server) {
        Game2SavedData data = server.overworld().getDataStorage().computeIfAbsent(tag-> load(tag,server), Game2SavedData::new, "xty_game2_saved_data");
        data.server = server;
        return data;
    }
    public static Game2SavedData getInstance(MinecraftServer server) {
        return ((SavedDataGetter) ((ServerEC) server).endinglib$serverECData()).getMap1game2SavedData();
    }
    public static Game2SavedData load(CompoundTag tag, MinecraftServer server) {
        Game2SavedData data = new Game2SavedData();
        if (CompoundTagUtils.containsBoolean(tag, "Stopped"))
            data.isStopped = tag.getBoolean("Stopped");
        if (CompoundTagUtils.containsBoolean(tag, "SceneChanging"))
            data.sceneChanging = tag.getBoolean("SceneChanging");
        if (CompoundTagUtils.containsString(tag, "HealthObjective"))
            data.healthScoreObjectiveName = tag.getString("HealthObjective");
        if (CompoundTagUtils.containsString(tag, "MaxHealthObjective"))
            data.maxHealthScoreObjectiveName = tag.getString("MaxHealthObjective");
        if (CompoundTagUtils.containsBoolean(tag, "DisableBlockInteraction"))
            data.disableBlockInteraction = tag.getBoolean("DisableBlockInteraction");

        if (CompoundTagUtils.containsCompound(tag, "Game1Functions"))
            data.game2Functions = Game2Functions.load(tag.getCompound("Game1Functions"), server, data);
        return data;
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putBoolean("Stopped", this.isStopped);
        tag.putBoolean("SceneChanging", this.sceneChanging);
        tag.putBoolean("DisableBlockInteraction", this.disableBlockInteraction);
        CompoundTag functions = new CompoundTag();
        if (this.game2Functions != null) this.game2Functions.save(functions);
        tag.put("Game1Functions", functions);
        if (this.healthScoreObjectiveName != null && !this.healthScoreObjectiveName.isEmpty())
            tag.putString("HealthObjective", this.healthScoreObjectiveName);
        if (this.maxHealthScoreObjectiveName != null && !this.maxHealthScoreObjectiveName.isEmpty())
            tag.putString("MaxHealthObjective", this.maxHealthScoreObjectiveName);
        return tag;
    }

    public void setStopped(boolean stopped) {
        if (this.isStopped != stopped) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CGame2StatsPacket(stopped, this.sceneChanging), serverPlayer);
        }
        isStopped = stopped;
    }
    public boolean isStopped() {
        return isStopped;
    }
    public void setDisableBlockInteraction(boolean d) {
        if (this.disableBlockInteraction != d) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CDisableBIPacket(d), serverPlayer);
        }
        disableBlockInteraction = d;
    }
    public boolean isDisableBlockInteraction() {
        return disableBlockInteraction;
    }

    public void setSceneChanging(boolean changing) {
        if (this.sceneChanging != changing) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CGame2StatsPacket(this.isStopped, changing), serverPlayer);
        }
        sceneChanging = changing;
    }
    public boolean isSceneChanging() {
        return sceneChanging;
    }

    public void setHealthScoreObjective(Objective objective) {
        this.healthScoreObjectiveName = objective.getName();
        this.setDirty();
    }
    public void resetHealthScoreObjective(Objective objective) {
        this.healthScoreObjectiveName = null;
        this.setDirty();
    }

    public @Nullable String getHealthScoreObjectiveName() {
        return healthScoreObjectiveName;
    }

    @Nullable
    public Objective getHealthObjective() {
        if (this.healthScoreObjectiveName == null || this.healthScoreObjectiveName.isEmpty())
            return null;
        return this.server.getScoreboard().getObjective(this.healthScoreObjectiveName);
    }

    public void setMaxHealthScoreObjective(Objective objective) {
        this.maxHealthScoreObjectiveName = objective.getName();
        this.setDirty();
    }
    public void resetMaxHealthScoreObjective(Objective objective) {
        this.maxHealthScoreObjectiveName = null;
        this.setDirty();
    }

    public @Nullable String getMaxHealthScoreObjectiveName() {
        return maxHealthScoreObjectiveName;
    }

    @Nullable
    public Objective getMaxHealthObjective() {
        if (this.maxHealthScoreObjectiveName == null || this.maxHealthScoreObjectiveName.isEmpty())
            return null;
        return this.server.getScoreboard().getObjective(this.maxHealthScoreObjectiveName);
    }

    public Game2Functions getGame2Functions() {
        return game2Functions;
    }
}
