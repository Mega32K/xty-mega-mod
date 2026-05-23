package com.mega.map.client.shader;

import com.mega.map.MegaMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ShaderRegisterEvent {
    @SubscribeEvent
    public static void onShaderRegistering(RegisterShadersEvent event) {
        registerShader(event, "position_tex", DefaultVertexFormat.POSITION_TEX, ModShaders::setMapPositionTex);
        registerShader(event, "health_background", DefaultVertexFormat.POSITION_TEX, ModShaders::setMapHealthBackground);
        registerShader(event, "map2_start", DefaultVertexFormat.POSITION_TEX, ModShaders::setMap2Start);
        registerShader(event, "dissolve_2d", DefaultVertexFormat.POSITION_TEX, ModShaders::setDissolve2d);
        registerShader(event, "rgb_outline", DefaultVertexFormat.POSITION_TEX, ModShaders::setRgbOutline);
        registerShader(event, "voronoi_flow_bg", DefaultVertexFormat.POSITION_TEX, ModShaders::setVoronoiFlowBackground);
        registerShader(event, "alpha_filter", DefaultVertexFormat.POSITION_TEX, ModShaders::setAlphaFilter);
        registerShader(event, "x_reverse", DefaultVertexFormat.POSITION_TEX, ModShaders::setXReverse);
        registerShader(event, "gui_blur_rect", DefaultVertexFormat.POSITION_TEX, ModShaders::setGuiBlurRect);
        registerShader(event, "logo_glitch", DefaultVertexFormat.POSITION_TEX, ModShaders::setLogoGlitch);
        registerShader(event, "logo_glitch_position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, ModShaders::setLogoGlitchPositionColorTexLightmap);
        registerShader(event, "logo_glitch_text_intensity_position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, ModShaders::setLogoGlitchTextIntensityPositionColorTexLightmap);
    }

    private static void registerShader(RegisterShadersEvent event, String name, VertexFormat format, Consumer<ShaderInstance> onLoaded) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, name);
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), id, format), onLoaded);
        } catch (Exception exception) {
            MegaMod.LOGGER.warn("Failed to load shader {}. The related visual effect will be disabled.", id, exception);
        }
    }
}
