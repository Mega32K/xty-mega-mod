package com.mega.map.common.data.map2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Map2Functions {
    private final Map2SavedData savedData;
    private final List<FunctionInstance> functionInstances = new ObjectArrayList<>();
    private final FunctionInstance startFunction = new FunctionInstance("start", this);
    private final FunctionInstance stopFunction = new FunctionInstance("stop", this);
    private final FunctionInstance countdownStop = new FunctionInstance("countdownStop", this);
    public Map2Functions(Map2SavedData savedData) {
        this.savedData = savedData;
    }

    public static Map2Functions load(CompoundTag tag, MinecraftServer server, Map2SavedData data) {
        Map2Functions map2Functions = new Map2Functions(data);
        for (FunctionInstance f : map2Functions.functionInstances)
            f.load(tag);
        return map2Functions;
    }
    public void save(CompoundTag tag) {
        for (FunctionInstance f : functionInstances)
            f.save(tag);
    } 
    public @Nullable String getCountdownStopFunction() {
        return countdownStop.getFunction();
    }

    public @Nullable String getStartFunction() {
        return startFunction.getFunction();
    }

    public void setStartFunction(String startFunction) {
        this.startFunction.setFunction(startFunction);
        this.savedData.setDirty();
    }

    public @Nullable String getStopFunction() {
        return stopFunction.getFunction();
    }

    public void setStopFunction(String stopFunction) {
        this.stopFunction.setFunction(stopFunction);
        this.savedData.setDirty();
    }
    public void setCountdownStopFunction(String jumpFunction) {
        this.countdownStop.setFunction(jumpFunction);
        this.savedData.setDirty();
    }
    public static class FunctionInstance {
        private final String serializeName;
        @Nullable
        private String function;

        public FunctionInstance(String serializeName, Map2Functions functions) {
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
