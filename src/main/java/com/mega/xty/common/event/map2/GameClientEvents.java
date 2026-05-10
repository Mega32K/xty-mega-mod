package com.mega.xty.common.event.map2;

import com.mega.xty.client.shader.ModShaders;
import com.mega.xty.client.shader.post.fps.Aspect43PostEffect;
import com.mega.xty.client.shader.post.map2.Game2StartPostEffect;
import com.mega.xty.common.data.fps.RoundStartData;
import com.mega.xty.common.data.map2.ClientGame2Data;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

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
