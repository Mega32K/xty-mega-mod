package com.mega.xty;

import com.mega.endinglib.util.EarlyConfig;
import com.mega.endinglib.util.mixin.ApplyCheckMixinConfigPlugin;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CustomMixinPlugin extends ApplyCheckMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals("com.mega.xty.mixin.gd656killicon.Gd656killiconMixin")) {
            return EarlyConfig.modIds.contains("gd656killicon") && EarlyConfig.modIds.contains("tacz");
        }
        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }
}
