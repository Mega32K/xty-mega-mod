package com.mega.xty.common.event;

import com.mega.endinglib.api.event.client.RenderShadowEvent;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.mixin.accessor.AccessorEntity;
import com.mega.endinglib.util.time.TimeContext;
import com.mega.xty.common.event.map2.DeathCameraEffectHandler;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.RoundStartData;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.data.map2.DeathData;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.client.overlay.fps.HotbarOverlay;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.client.shader.post.map2.Game2StartPostEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventsHandler {
    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if ((event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id()) || event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id()) || event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id()) || event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id()) || event.getOverlay().id().equals(VanillaGuiOverlay.MOUNT_HEALTH.id()) || event.getOverlay().id().equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) && HotbarOverlay.shouldReplaceVanillaHotbar()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel clientLevel = mc.level;
            if (clientLevel != null) {
                {
                    if (mc.player != null && mc.gameMode != null) {
                        double d0 = mc.gameMode.getPickRange() + 2;
                        double d1;
                        d0 = d1 = Math.max(d0, mc.player.getEntityReach() + 2);
                        Entity entity = mc.cameraEntity;
                        if (entity != null) {
                            ClientGameData.pickedEntity = null;
                            ClientGameData.hitResult = null;
                            double entityReach = mc.player.getEntityReach() + 2; // Note - MC-76493 - We must validate players cannot click-through objects.
                            Vec3 vec3 = entity.getEyePosition(0.5F);
                            Vec3 vec31 = entity.getViewVector(1.0F);
                            Vec3 vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
                            AABB aabb = entity.getBoundingBox().expandTowards(vec31.scale(d0)).inflate(1.0D, 1.0D, 1.0D);
                            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(entity, vec3, vec32, aabb, (p_234237_) -> {
                                return !p_234237_.isSpectator() && p_234237_.isPickable();
                            }, d1);
                            if (entityhitresult != null) {
                                Entity entity1 = entityhitresult.getEntity();
                                Vec3 vec33 = entityhitresult.getLocation();
                                double d2 = vec3.distanceToSqr(vec33);
                                if (d2 > d1 || d2 > entityReach * entityReach) { // Discard if the result is behind a block, or past the entity reach max. The var "flag" no longer has a use.
                                    ClientGameData.hitResult = BlockHitResult.miss(vec33, Direction.getNearest(vec31.x, vec31.y, vec31.z), BlockPos.containing(vec33));
                                } else if (d2 < d1 || ClientGameData.hitResult == null) {
                                    ClientGameData.hitResult = entityhitresult;
                                    if (entity1 instanceof LivingEntity || entity1 instanceof ItemFrame || entity1 instanceof Interaction) {
                                        ClientGameData.pickedEntity = entity1;
                                    }
                                }
                            }
                        }
                    }
                }
                DeathCameraEffectHandler.clientTick();
                Game2StartPostEffect.clientTick();
                if (!mc.isPaused()) {
                    RoundStartData.tick();
                    ClientFpsData.tick();
                    ClientGame2Data.tick(clientLevel);
                }
                if (!ClientGameData.map2Playing()) {
                    if (!ClientGameData.toAddDeathData.isEmpty()) ClientGameData.toAddDeathData.clear();
                    if (!ClientGameData.toRemoveDeathData.isEmpty()) ClientGameData.toRemoveDeathData.clear();
                    if (!ClientGameData.deathDataList.isEmpty()) ClientGameData.deathDataList.clear();
                } else {
                    DeathData data;
                    while ((data = ClientGameData.toAddDeathData.poll()) != null) {
                        ClientGameData.deathDataList.add(data);
                    }
                    while ((data = ClientGameData.toRemoveDeathData.poll()) != null) {
                        ClientGameData.deathDataList.remove(data);
                    }
                    ClientGameData.deathDataList.forEach(DeathData::tick);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        if (shouldHideSoulInvisiblePlayer(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        float partialTicks = event.getPartialTick();
        CommonProxy.getXtyCap(event.getEntity()).ifPresent(capability -> {
            if (capability.canUsePartialTeleportAnim) {
                Player player = event.getEntity();
                PoseStack poseStack = event.getPoseStack();
                double d0 = Mth.lerp(partialTicks, player.xOld, player.getX());
                double d1 = Mth.lerp(partialTicks, player.yOld, player.getY());
                double d2 = Mth.lerp(partialTicks, player.zOld, player.getZ());
                poseStack.translate(-d0, -d1, -d2);
                d0 = Mth.lerp(capability.calculateInterpolationProgress(TimeContext.Client.alwaysPartial()), capability.smoothStartPos.x, capability.smoothTeleportTarget.x);
                d1 = Mth.lerp(capability.calculateInterpolationProgress(TimeContext.Client.alwaysPartial()), capability.smoothStartPos.y, capability.smoothTeleportTarget.y);
                d2 = Mth.lerp(capability.calculateInterpolationProgress(TimeContext.Client.alwaysPartial()), capability.smoothStartPos.z, capability.smoothTeleportTarget.z);
                ((AccessorEntity) player).setPositionField(new Vec3(d0, d1, d2));
                poseStack.translate(d0, d1, d2);
            }
        });
    }
    @SubscribeEvent
    public static void renderShadowEvent(RenderShadowEvent event) {
        if (event.getEntity() instanceof AbstractClientPlayer cp) {
            if (shouldHideSoulInvisiblePlayer(cp)) {
                event.setCanceled(true);
                return;
            }
            if (cp.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get())) {
                CommonProxy.getMap2Cap(cp).ifPresent(cap -> {
                    Player localPlayer = ClientWrapped.clientPlayer();
                    if (localPlayer != cp) {
                        if (localPlayer != null) {
                            if ((!localPlayer.isAlliedTo(cp) && !localPlayer.isSpectator())) {
                                event.setCanceled(true);
                            }
                        }
                    } else event.setCanceled(true);
                });
            }
        }
    }

    private static boolean shouldHideSoulInvisiblePlayer(Player player) {
        Player localPlayer = ClientWrapped.clientPlayer();
        if (localPlayer == null || localPlayer == player) {
            return false;
        }
        if (localPlayer.isCreative() || localPlayer.isSpectator() || localPlayer.isAlliedTo(player)) {
            return false;
        }
        return CommonProxy.getMap2Cap(player)
                .map(cap -> cap.getSoulInvisible() >= 15)
                .orElse(false);
    }
}
