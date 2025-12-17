package com.mega.xty.proxy;

import com.google.common.collect.Queues;
import com.mega.endinglib.api.client.levelevent.LevelEventManager;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.client.RenderUtils;
import com.mega.xty.client.overlay.DebugOverlays;
import com.mega.xty.client.text.ClientItemDisplayTooltip;
import com.mega.xty.client.text.ItemDisplayTooltip;
import com.mega.xty.common.init.EntityInit;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ClientProxy implements ModProxy {
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
        mBus.addListener(this::onShaderRegistering);
        mBus.addListener(this::onEntityRendererRegistering);
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LevelEventManager.registerLevelEvent(110120, (blockPos, randomSource, i) -> {
                Minecraft mc = Minecraft.getInstance();
                switch (i) {
                    case 0 -> {
                        isUpdatingNoCullingInfo = true;
                        try {
                            chunksNoCullingBlocks.add(new BlockPos(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ())));
                            chunksNoCullingBlocks2.add(new BlockPos(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ())));
                        } finally {
                            isUpdatingNoCullingInfo = false;
                        }
                    }
                    case 1 -> {
                        isUpdatingNoCullingInfo = true;
                        try {
                            chunksNoCullingBlocks.remove(new BlockPos(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ())));
                            chunksNoCullingBlocks2.remove(new BlockPos(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ())));
                        } finally {
                            isUpdatingNoCullingInfo = false;
                        }
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
    private void onShaderRegistering(RegisterShadersEvent event) {

    }
    private void onEntityRendererRegistering(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.BINDING.get(), NoopRenderer::new);
    }

}
