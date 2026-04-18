package com.mega.xty.client.shader;

import com.mega.xty.XtyMegaMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ShaderRegisterEvent {
    @SubscribeEvent
    public static void onShaderRegistering(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "position_tex"), DefaultVertexFormat.POSITION_TEX), ModShaders::setMapPositionTex);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "health_background"), DefaultVertexFormat.POSITION_TEX), ModShaders::setMapHealthBackground);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "map2_start"), DefaultVertexFormat.POSITION_TEX), ModShaders::setMap2Start);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "dissolve_2d"), DefaultVertexFormat.POSITION_TEX), ModShaders::setDissolve2d);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "rgb_outline"), DefaultVertexFormat.POSITION_TEX), ModShaders::setRgbOutline);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "voronoi_flow_bg"), DefaultVertexFormat.POSITION_TEX), ModShaders::setVoronoiFlowBackground);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "alpha_filter"), DefaultVertexFormat.POSITION_TEX), ModShaders::setAlphaFilter);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "x_reverse"), DefaultVertexFormat.POSITION_TEX), ModShaders::setXReverse);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "grayscale_position_tex"), DefaultVertexFormat.POSITION_TEX), ModShaders::setGray);
    }
}
