package com.mega.map.mixin.map1.game_2;

import com.mega.map.common.data.map1.Game2SavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Spider.class)
public abstract class Spider1Mixin extends Monster {

    protected Spider1Mixin(EntityType<? extends Monster> p_33002_, Level p_33003_) {
        super(p_33002_, p_33003_);
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        if (this.level() instanceof ServerLevel serverLevel) {
            if (!Game2SavedData.getInstance(serverLevel.getServer()).isStopped())
                return 0F;
        }
        return super.getLightLevelDependentMagicValue();
    }
}
