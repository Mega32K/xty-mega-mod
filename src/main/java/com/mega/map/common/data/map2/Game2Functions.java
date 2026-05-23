package com.mega.map.common.data.map2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Game2Functions {
    private final Game2SavedData savedData;
    private final List<FunctionInstance> functionInstances = new ObjectArrayList<>();
    private final FunctionInstance onPlayerDeathFunction = new FunctionInstance("onPlayerDeath", this);
    private final FunctionInstance startFunction = new FunctionInstance("start", this);
    private final FunctionInstance startNewRoundFunction = new FunctionInstance("startNewRound", this);
    private final FunctionInstance stopFunction = new FunctionInstance("stop", this);
    public Game2Functions(Game2SavedData savedData) {
        this.savedData = savedData;
    }

    public static Game2Functions load(CompoundTag tag, MinecraftServer server, Game2SavedData data) {
        Game2Functions game1Functions = new Game2Functions(data);
        for (FunctionInstance f : game1Functions.functionInstances)
            f.load(tag);
        return game1Functions;
    }
    public void save(CompoundTag tag) {
        for (FunctionInstance f : functionInstances)
            f.save(tag);
    }

    public @Nullable String getOnPlayerDeathFunction() {
        return onPlayerDeathFunction.getFunction();
    }

    public void setOnPlayerDeathFunction(String onPlayerDeathFunction) {
        this.onPlayerDeathFunction.setFunction(onPlayerDeathFunction);
        this.savedData.setDirty();
    }

    public @Nullable String getStartFunction() {
        return startFunction.getFunction();
    }

    public void setStartFunction(String startFunction) {
        this.startFunction.setFunction(startFunction);
        this.savedData.setDirty();
    }

    public @Nullable String getStartNewRoundFunction() {
        return startNewRoundFunction.getFunction();
    }

    public void setStartNewRoundFunction(String startNewRoundFunction) {
        this.startNewRoundFunction.setFunction(startNewRoundFunction);
        this.savedData.setDirty();
    }

    public @Nullable String getStopFunction() {
        return stopFunction.getFunction();
    }

    public void setStopFunction(String stopFunction) {
        this.stopFunction.setFunction(stopFunction);
        this.savedData.setDirty();
    }
    public static class FunctionInstance {
        private final String serializeName;
        @Nullable
        private String function;

        public FunctionInstance(String serializeName, Game2Functions functions) {
            this.serializeName = serializeName;
            functions.functionInstances.add(this);
        }
        public void save(CompoundTag tag) {
            if (this.function != null && !this.function.isEmpty()) {
                tag.putString(serializeName, this.function);
            }
        }
        public void load(CompoundTag tag) {
            this.function = tag.getString(serializeName);
        }

        public @Nullable String getFunction() {
            return function;
        }

        public void setFunction(String function) {
            this.function = function;
        }
    }
}
