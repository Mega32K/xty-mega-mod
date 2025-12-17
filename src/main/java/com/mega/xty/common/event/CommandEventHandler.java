package com.mega.xty.common.event;

import com.mega.xty.common.capability.Limbs;
import com.mega.xty.common.item.armor.WineBottleHat;
import com.mega.xty.proxy.CommonProxy;
import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandEventHandler {
    @SubscribeEvent
    public static void onHurtEvent(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack bottleStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if (bottleStack.getItem() instanceof WineBottleHat) {
                DamageSource damageSource = event.getSource();
                if (damageSource.getDirectEntity() instanceof Projectile && damageSource.getSourcePosition() != null) {
                    Vector3f relativeDamagePos = damageSource.getSourcePosition().subtract(player.position().add(0,player.getBoundingBox().getYsize()/2F,0)).toVector3f();
                    Quaternionf rotation = new Quaternionf(0, 0, 0, 1);
                    rotation.rotateYXZ(-player.yBodyRot * Mth.DEG_TO_RAD, player.getXRot() * Mth.DEG_TO_RAD, 0.0F);
                    Vector3f body = new Vector3f(0, 0, 0).rotate(rotation);
                    Vector3f leftHand = new Vector3f(.5F, 0, 0).rotate(rotation);
                    Vector3f rightHand = new Vector3f(-.5F, 0, 0).rotate(rotation);
                    Vector3f leftLeg = new Vector3f(.25F, -.325f, 0).rotate(rotation);
                    Vector3f rightLeg = new Vector3f(-.25F, -.325f, 0).rotate(rotation);
                    Float2ObjectArrayMap<Limbs> map = new Float2ObjectArrayMap<>(4);
                    map.put(leftHand.distance(relativeDamagePos), Limbs.LEFT_HAND);
                    map.put(rightHand.distance(relativeDamagePos), Limbs.RIGHT_HAND);
                    map.put(leftLeg.distance(relativeDamagePos), Limbs.LEFT_LEG);
                    map.put(rightLeg.distance(relativeDamagePos), Limbs.RIGHT_LEG);
                    map.put(body.distance(relativeDamagePos), Limbs.BODY);
                    Limbs limbs = null;
                    float distance = Float.MAX_VALUE;
                    for (var entry : map.float2ObjectEntrySet()) {
                        if (distance >= entry.getFloatKey()) {
                            distance = entry.getFloatKey();
                            limbs = entry.getValue();
                        }
                    }
                    if (limbs != null && limbs != Limbs.BODY) {
                        Limbs finalLimbs = limbs;
                        CommonProxy.getXtyCap(player).ifPresent(cap -> cap.setLimbDisabled(finalLimbs, true));
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void disabledLimbsAttack(AttackEntityEvent event) {
        CommonProxy.getXtyCap(event.getEntity()).ifPresent(cap-> {
            if (cap.isLimbDisabled(Limbs.RIGHT_HAND))
                event.setCanceled(true);
        });
    }
}
