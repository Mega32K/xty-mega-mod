package com.mega.map.mixin.map2.game2;

import com.mega.map.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "playerTouch(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true)
    private void preventBlueTeamPickingC4(Player player, CallbackInfo ci) {
        if (player.getTeam() == null || player.getTeam().getColor() != ChatFormatting.BLUE) {
            return;
        }
        ItemStack stack = ((ItemEntity) (Object) this).getItem();
        if (stack.is(ItemInit.C4_BOMB.get())) {
            ci.cancel();
        }
    }
}
