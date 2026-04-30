package com.mega.xty.common.event;

import com.mega.endinglib.api.event.client.RenderShadowEvent;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.mixin.accessor.AccessorEntity;
import com.mega.endinglib.util.time.TimeContext;
import com.mega.xty.common.event.map2.DeathCameraEffectHandler;
import com.mega.xty.common.data.fps.ClientFpsData;
import com.mega.xty.common.data.fps.RoundStartData;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.data.map2.DeathData;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.client.overlay.fps.HotbarOverlay;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.client.shader.post.map2.Game2StartPostEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventsHandler {
    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id()) && HotbarOverlay.shouldReplaceVanillaHotbar()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel clientLevel = mc.level;
            if (clientLevel != null) {
                DeathCameraEffectHandler.clientTick();
                Game2StartPostEffect.clientTick();
                if (!mc.isPaused()) {
                    RoundStartData.tick();
                    ClientFpsData.tick();
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
