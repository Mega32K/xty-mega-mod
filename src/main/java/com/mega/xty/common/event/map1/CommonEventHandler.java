package com.mega.xty.common.event.map1;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.mixin.accessor.AccessorCommandSourceStack;
import com.mega.xty.common.capability.Limbs;
import com.mega.xty.common.item.armor.WineBottleHat;
import com.mega.xty.common.tags.XtyDamageTypeTags;
import com.mega.xty.mixin.AccessorAbstractArrow;
import com.mega.xty.proxy.CommonProxy;
import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onHurtEvent(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack bottleStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if (bottleStack.getItem() instanceof WineBottleHat) {
                DamageSource damageSource = event.getSource();
                if (damageSource.getDirectEntity() instanceof Projectile && damageSource.getSourcePosition() != null) {
                    CompoundTag itemNbt = bottleStack.getOrCreateTag();
                    Vector3f relativeDamagePos = damageSource.getSourcePosition().subtract(player.position().add(0,player.getBoundingBox().getYsize()/2F,0)).toVector3f();
                    Quaternionf rotation = new Quaternionf(0, 0, 0, 1);
                    rotation.rotateYXZ(-player.yBodyRot * Mth.DEG_TO_RAD, player.getXRot() * Mth.DEG_TO_RAD, 0.0F);
                    Vector3f body = new Vector3f(0, 0.24f, 0).rotate(rotation);
                    Vector3f leftHand = new Vector3f(.5F, 0, 0).rotate(rotation);
                    Vector3f rightHand = new Vector3f(-.5F, 0, 0).rotate(rotation);
                    Vector3f leftLeg = new Vector3f(.25F, -.2f, 0).rotate(rotation);
                    Vector3f rightLeg = new Vector3f(-.25F, -.2f, 0).rotate(rotation);
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
                        CommonProxy.getXtyCap(player).ifPresent(cap -> {
                            String tagName = finalLimbs.getName() + "Command";
                            if (CompoundTagUtils.containsString(itemNbt, tagName) && !cap.isLimbDisabled(finalLimbs)) {
                                if (player.level() instanceof ServerLevel serverLevel) {
                                    CommandSourceStack css = player.createCommandSourceStack();
                                    ((AccessorCommandSourceStack) css).setSilent(true);
                                    serverLevel.getServer().getCommands().performPrefixedCommand(css, itemNbt.getString(tagName));
                                }
                            }
                        });
                    }
                    if (limbs != null) {
                        Limbs finalLimbs = limbs;
                        CommonProxy.getXtyCap(player).ifPresent(cap -> {
                            if (cap.isLimbDisabled(Limbs.LEFT_LEG) && cap.isLimbDisabled(Limbs.RIGHT_LEG) && cap.isLimbDisabled(Limbs.LEFT_HAND) && cap.isLimbDisabled(Limbs.RIGHT_HAND)) {
                                if (finalLimbs == Limbs.BODY) {
                                    String tagName = finalLimbs.getName() + "Command";
                                    if (CompoundTagUtils.containsString(itemNbt, tagName) && !cap.isLimbDisabled(finalLimbs)) {
                                        if (player.level() instanceof ServerLevel serverLevel) {
                                            CommandSourceStack css = player.createCommandSourceStack();
                                            ((AccessorCommandSourceStack) css).setSilent(true);
                                            serverLevel.getServer().getCommands().performPrefixedCommand(css, itemNbt.getString(tagName));
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void fallingInvulnerable(LivingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!event.getSource().is(XtyDamageTypeTags.GAME_INVULNERABLE_BYPASS))
                CommonProxy.getXtyCap(player).ifPresent(capability -> {
                    if (capability.isGameInvul()) {
                        event.setCanceled(true);
                    }
                });
        }
    }
    @SubscribeEvent
    public static void disabledLimbsAttack(AttackEntityEvent event) {
        CommonProxy.getXtyCap(event.getEntity()).ifPresent(cap-> {
            if (cap.isLimbDisabled(Limbs.RIGHT_HAND))
                event.setCanceled(true);
        });
    }
    @SubscribeEvent
    public static void onHitOtherCommand(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack bottleStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if (bottleStack.getItem() instanceof WineBottleHat) {
                CompoundTag itemNbt = bottleStack.getOrCreateTag();
                DamageSource damageSource = event.getSource();
                if (damageSource.getDirectEntity() instanceof Projectile && damageSource.getSourcePosition() != null) {
                    if (CompoundTagUtils.containsString(itemNbt, "HitOtherCommand")) {

                        if (player.level() instanceof ServerLevel serverLevel && player.isAlive()) {
                            if (damageSource.getEntity() instanceof Player sp) {
                                player.setLastHurtByMob(sp);
                                player.setLastHurtByPlayer(sp);
                            }
                            if (damageSource.getDirectEntity() instanceof AbstractArrow abstractArrow) {
                                ((AccessorAbstractArrow) abstractArrow).callDoPostHurtEffects(player);
                            }
                            CommandSourceStack css = player.createCommandSourceStack().withSuppressedOutput();
                            serverLevel.getServer().getCommands().performPrefixedCommand(css, itemNbt.getString("HitOtherCommand"));
                            if (!(event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))) {

                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }
}
