package com.mega.xty.proxy;

import com.google.gson.JsonSyntaxException;
import com.mega.endinglib.api.client.levelevent.LevelEventManager;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.MapLevelEvents;
import com.mega.xty.client.overlay.DebugOverlays;
import com.mega.xty.client.overlay.loading.MegaStyleLoadingEffect;
import com.mega.xty.client.overlay.map1.HealthOverlay;
import com.mega.xty.client.overlay.map2.*;
import com.mega.xty.client.renderer.entity.BlackHoleRenderer;
import com.mega.xty.client.renderer.entity.Game2ItemRenderer;
import com.mega.xty.client.renderer.entity.ShadowPlayerRenderer;
import com.mega.xty.client.renderer.entity.WrappedPlayerRenderer;
import com.mega.xty.client.screen.map2.GameStartScreen;
import com.mega.xty.client.screen.map2.RenameScreen;
import com.mega.xty.client.shader.ModShaders;
import com.mega.xty.client.text.ClientItemDisplayTooltip;
import com.mega.xty.client.text.ItemDisplayTooltip;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.entity.ShadowPlayerEntity;
import com.mega.xty.common.init.EntityInit;
import com.mega.xty.common.init.ParticleInit;
import com.mega.xty.common.particle.Game2HitParticle;
import com.mega.xty.util.data_expand.ExtraPlayerRenderer;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ClientProxy implements ModProxy {
    public static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/icons.png");
    public static final ResourceLocation DEATH_PLAYER_SKIN = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/entity/death.png");
    public static volatile boolean isUpdatingNoCullingInfo;
    public static List<BlockPos> chunksNoCullingBlocks = new ObjectArrayList<>();
    public static Set<BlockPos> chunksNoCullingBlocks2 = new ObjectOpenHashSet<>();
    public static final KeyMapping DEBUG_ITEM_GUI = new KeyMapping("key.xtymegamod.debug_item_gui", GLFW.GLFW_KEY_G | GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.xtymegamod");
    public ClientProxy() {
        IEventBus mBus = this.getModBus();
        IEventBus fBus = this.getForgeBus();
        mBus.addListener(this::onKeyRegister);
        mBus.addListener(this::clientSetup);
        mBus.addListener(this::onRegisterOverlays);
        mBus.addListener(this::onRegisterClientTooltipComponent);
        mBus.addListener(this::onEntityRendererRegistering);
        mBus.addListener(this::registerParticleProviders);
        mBus.addListener(this::registerOverlay);
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            ReloadableResourceManager manager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
            manager.registerReloadListener(new ResourceManagerReloadListener() {
                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    ResourceLocation path = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "shaders/post/motion_blur.json");
                    try {
                        if (MegaStyleLoadingEffect.motionEffect != null)
                            MegaStyleLoadingEffect.motionEffect.close();
                        Minecraft minecraft = Minecraft.getInstance();
                        Window window = minecraft.getWindow();
                        PostChain postChain = new PostChain(minecraft.getTextureManager(), resourceManager, minecraft.getMainRenderTarget(), path);
                        postChain.resize(window.getWidth(), window.getHeight());
                        MegaStyleLoadingEffect.motionEffect = postChain;
                    } catch (JsonSyntaxException jsonE) {
                        XtyMegaMod.LOGGER.warn("Failed to parse shader: {}", path, jsonE);
                    } catch (IOException IOE) {
                        XtyMegaMod.LOGGER.warn("Failed to load shader: {}", path, IOE);
                    }
                }
            });
            LevelEventManager.registerLevelEvent(110120, (blockPos, randomSource, i) -> {
                switch (i) {
                    case 2 -> {
                        ClientProxy.playSoundNoDelayed(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 2.5F, 1F, true, randomSource.nextLong());
                    }
                    case MapLevelEvents.FPS_SPECTATE -> {
                        ClientGameData.currentCameraPlayerIndex = Math.max(ClientGameData.currentCameraPlayerIndex, 0);
                        ClientGameData.fpsSpectate();
                    }
                }
            });
        });
    }
    private void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(DEBUG_ITEM_GUI);
    }
    private void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("xty_debug", DebugOverlays.INSTANCE);
    }
    private void onRegisterClientTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ItemDisplayTooltip.class, ClientItemDisplayTooltip::new);
    }
    private void onEntityRendererRegistering(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.BINDING.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityInit.THROWN_ITEM.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityInit.SHADOW_PLAYER.get(), ShadowPlayerRenderer::new);
        event.registerEntityRenderer(EntityInit.BLACKHOLE.get(), BlackHoleRenderer::new);
        event.registerEntityRenderer(EntityInit.GAME_ITEM.get(), Game2ItemRenderer::new);
    }
    private void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleInit.GAME2_HIT.get(), Game2HitParticle.Provider::new);
    }
    private void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "game2_health", new HealthOverlay());
        event.registerAboveAll("map2/health", new com.mega.xty.client.overlay.map2.HealthOverlay());
        event.registerAboveAll("map2/killcount", new KillCountOverlay());
        event.registerAbove(
                ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "map2/killcount"),
                "map2/death_data",
                new DeathDataOverlay()
        );
        event.registerAboveAll("map2/ghost_circle", new GhostCircleOverlay());
        event.registerAboveAll("map2/points", new PointsOverlay());
        event.registerAboveAll("fps/select_player", new SelectPlayerOverlay());
        event.registerAboveAll("fps/tab", new TabOverlay());
        event.registerAboveAll("map2/text_tip", new TextTipOverlay());
    }
    public static void setObj(ShadowPlayerEntity entity, Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            Minecraft mc = Minecraft.getInstance();
            EntityRenderer<? super Player> renderer = mc.getEntityRenderDispatcher().getRenderer(player);
            if ((Object)(renderer) instanceof PlayerRenderer playerRenderer) {
                entity.renderer = new WrappedPlayerRenderer(
                        new EntityRendererProvider.Context(mc.getEntityRenderDispatcher(),mc.getItemRenderer(),mc.getBlockRenderer(),mc.gameRenderer.itemInHandRenderer, mc.getResourceManager(), mc.getEntityModels(), mc.font),
                        ((ExtraPlayerRenderer) playerRenderer).xtyMegaMod$isSlim()
                );
            }
            entity.sWalkSpeed = player.walkAnimation.speed(mc.getPartialTick());
            entity.sWalkPosition = player.walkAnimation.position(mc.getPartialTick());
            entity.sAttackAnim = player.getAttackAnim(mc.getPartialTick());
        }
    }

    public static void playSoundNoDelayed(double p_233603_, double p_233604_, double p_233605_, SoundEvent p_233606_, SoundSource p_233607_, float p_233608_, float p_233609_, boolean p_233610_, long p_233611_) {
        Minecraft mc = Minecraft.getInstance();
        SimpleSoundInstance simplesoundinstance = new SimpleSoundInstance(p_233606_, p_233607_, p_233608_, p_233609_, RandomSource.create(p_233611_), p_233603_, p_233604_, p_233605_);
        mc.getSoundManager().play(simplesoundinstance);
    }
    public static void openRenameScreen(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            if (mc.level.getPlayerByUUID(uuid) instanceof AbstractClientPlayer cp) {
                mc.execute(()-> {
                    mc.forceSetScreen(new RenameScreen(cp));
                });
            }
        }
    }
    public static void openMap2StartScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof GameStartScreen))
            mc.execute(() ->{
                mc.forceSetScreen(new GameStartScreen(Component.empty()));
            });
    }
    public static void closeMap2Screen() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.screen instanceof GameStartScreen || mc.screen instanceof RenameScreen)
                mc.setScreen(null);
        });
    }
}
