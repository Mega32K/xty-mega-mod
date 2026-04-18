package com.mega.xty.mixin.map2.game2;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow private Level level;

    @Shadow public abstract boolean isInvisibleTo(Player p_20178_);

    @Shadow @Nullable public abstract Team getTeam();

    @Inject(method = "isInvisibleTo", at = @At("RETURN"), cancellable = true)
    private void opticalInvisible(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if ((Object)this instanceof Player player) {
                CommonProxy.getMap2Cap(player).ifPresent(cap -> {
                    if (cap.getSoulInvisible() >= 15) {
                        if (level.isClientSide) {
                            Player localPlayer = ClientWrapped.clientPlayer();
                            if (localPlayer.isSpectator()) {
                                cir.setReturnValue(false);
                            } else {
                                Team team = this.getTeam();
                                cir.setReturnValue(team == null || localPlayer.getTeam() != team || !team.canSeeFriendlyInvisibles());
                            }
                        } else cir.setReturnValue(true);
                    }
                });
            }
        }
    }
}
