package com.mega.map.common.event.map2;

import com.mega.map.client.shader.ModShaders;
import com.mega.map.client.shader.post.map2.Game2StartPostEffect;
import com.mega.map.common.data.fps.RoundStartData;
import com.mega.map.common.data.map2.ClientGame2Data;
import com.mega.map.common.data.map2.ClientGameData;
import com.mega.map.common.init.ItemInit;
import com.mega.map.proxy.CommonProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GameClientEvents {
    @SubscribeEvent
    public static void onRenderRightHand(RenderHandEvent event) {
        if (ClientGameData.isStopped) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            if (cap.isXaeroDead()) {
                event.setCanceled(true);
            }
        });
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderOpticalSuitName(RenderNameTagEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (event.getEntity() instanceof AbstractClientPlayer cp && !player.isAlliedTo(cp)) {
            if (cp.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get())) {
                CommonProxy.getMap2Cap(cp).ifPresent(cap -> {
                    event.setResult(Event.Result.DENY);
                    float invisible = cap.getInvisibleValue(event.getPartialTick());
                    if (player == cp || player.isSpectator())
                        invisible = Math.max(0.15F, invisible);
                    if (cp.shouldShowName()) {
                        renderNameTag(Minecraft.getInstance().getEntityRenderDispatcher(), event.getEntityRenderer(), cp, event.getContent(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), invisible * 2F);
                    }
                });
            }
        }
    }
    private static void renderNameTag(EntityRenderDispatcher entityRenderDispatcher, EntityRenderer<?> renderer, AbstractClientPlayer entity, Component context, PoseStack poseStack, MultiBufferSource bufferSource, int light, float alpha) {
        if (alpha == 0.0F) return;
        double d0 = entityRenderDispatcher.distanceToSqr(entity);
        if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(entity, d0)) {
            boolean flag = !entity.isDiscrete();
            float f = entity.getNameTagOffsetY();
            poseStack.pushPose();
            poseStack.translate(0.0F, f, 0.0F);
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();
            float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * alpha;
            int j = (int)(f1 * 255.0F) << 24;
            Font font = renderer.getFont();
            float f2 = (float)(-font.width(context) / 2);
            font.drawInBatch(context, f2, 0, ((int)(alpha * 255.0F) << 24) | 0x00FFFFFF, false, matrix4f, bufferSource, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, light);
            if (flag) {
                font.drawInBatch(context, f2, 0, ((int)(alpha * 255.0F) << 24) | 0x00FFFFFF, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, light);
            }

            poseStack.popPose();
        }
    }
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null)
                ClientGameData.tick(Minecraft.getInstance().level);
            if (ClientGameData.isStopped) return;
            if (mc.getCameraEntity() instanceof AbstractClientPlayer clientPlayer) {
                if (ClientGameData.isTeamMode) {
                    int teamColor = clientPlayer.getTeamColor();
                    ModShaders.getMapHealthBackground().safeGetUniform("Color").set(new float[]{FastColor.ARGB32.red(teamColor) / 255F, FastColor.ARGB32.green(teamColor) / 255F, FastColor.ARGB32.blue(teamColor) / 255F, 1.0F});
                } else {
                    ModShaders.getMapHealthBackground().safeGetUniform("Color").set(new float[]{0.5F, 0.2F, 1.0F, 1.0F});
                }
            }
        }
    }
    @SubscribeEvent
    public static void onChangeCameraPlayer(InputEvent.MouseScrollingEvent event) {
        if (ClientGameData.isStopped) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        LocalPlayer player = mc.player;
        if (player == null) return;
        CommonProxy.getMap2Cap(player).ifPresent(cap -> {
            if (cap.isXaeroDead()) {
                List<AbstractClientPlayer> teamPlayers = ClientGameData.getSpectatablePlayers();
                if (teamPlayers.isEmpty()) {
                    ClientGameData.currentCameraPlayerIndex = -1;
                } else {
                    if (event.getScrollDelta() > 0) ClientGameData.currentCameraPlayerIndex++;
                    else ClientGameData.currentCameraPlayerIndex--;
                    if (ClientGameData.currentCameraPlayerIndex >= teamPlayers.size())
                        ClientGameData.currentCameraPlayerIndex = 0;
                    else if (ClientGameData.currentCameraPlayerIndex < 0)
                        ClientGameData.currentCameraPlayerIndex = teamPlayers.size() - 1;
                }
                ClientGameData.fpsSpectate();
            }
        });
    }
    @SubscribeEvent
    public static void postRendered(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            ClientGame2Data.postRenderedItemRenders.forEach(prir -> {
                prir.consumer().accept(prir.poseStack());
            });
            ClientGame2Data.postRenderedItemRenders.clear();
        }
    }
    @SubscribeEvent
    public static void onDisconnected(ClientPlayerNetworkEvent.LoggingOut event) {
        DeathCameraEffectHandler.stop();
        C4SpectateCameraHandler.stop();
        Game2StartPostEffect.stop();
        RoundStartData.stop();
        ClientGame2Data.setRoundStartLockedTicks(0);
        if (event.getMultiPlayerGameMode() != null) {
            ClientGameData.currentCameraPlayerIndex = 0;
            ClientGameData.aliveSameTeamPlayers.clear();
            ClientGameData.aliveSameTeamPlayersWithoutLocal.clear();
        }
    }
}
