package com.mega.map.mixin.lrtactical;

import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.map.common.entity.ThrownItemEntity;
import com.mega.map.common.init.ItemInit;
import com.mega.map.proxy.CommonProxy;
import me.xjqsh.lrtactical.entity.SmokeGrenadeEntity;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ModDependsMixin("lrtactical")
@Mixin(SmokeGrenadeEntity.class)
public abstract class SmokeGrenadeEntityMixin extends ThrowableItemEntity {
    SmokeGrenadeEntityMixin(EntityType<ThrownItemEntity> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void weakenOpticalNanoSuitAbility(CallbackInfo ci) {
        if (!level().isClientSide) {
            AABB checkBox = this.getBoundingBox().inflate(10.0);
            for(Player player : level().players()) {
                if (checkBox.contains(player.getX(), player.getY(), player.getZ()) && (EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player))) {
                    if (player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get())) {
                        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
                              cap.smokeAround = 2;
                        });
                    }
                }
            }
        }
    }
}
