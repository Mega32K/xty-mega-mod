package com.mega.map.common.event.map1;

import com.mega.endinglib.api.item.component.ItemComponentManager;
import com.mega.map.common.component.ComponentInit;
import com.mega.map.common.component.GunFireComponent;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IGun;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class TaczCommonEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFire(GunFireEvent event) {
        ItemStack stack = event.getGunItemStack();
        GunFireComponent component = ItemComponentManager.get(stack, ComponentInit.GUN_SHOOT);
        LivingEntity shooter = event.getShooter();
        if (component != null && event.getLogicalSide() == LogicalSide.SERVER && shooter.level() instanceof ServerLevel serverLevel) {
            component.apply(serverLevel, shooter, shooter.getMainHandItem().equals(stack) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (component.cancelShoot())
                event.setCanceled(true);
        }
    }
    public static boolean isItemStackGun(ItemStack stack) {
        return stack.getItem() instanceof IGun;
    }
}
