package com.mega.xty.client.shader;

import com.mega.xty.XtyMegaMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector4f;

public class ModShaders extends RenderType {
    public static final ResourceLocation NOISE = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/noise.png");
    private static ShaderInstance MAP_POSITION_TEX;
    private static ShaderInstance MAP_HEALTH_BACKGROUND;
    private static ShaderInstance MAP2_START;
    private static ShaderInstance DISSOLVE_2D;
    private static ShaderInstance RGB_OUTLINE;
    private static ShaderInstance VORONOI_FLOW_BACKGROUND;
    private static ShaderInstance GUI_BLUR_RECT;
    private static ShaderInstance LOGO_GLITCH;
    private static ShaderInstance LOGO_GLITCH_POSITION_COLOR_TEX_LIGHTMAP;
    private static ShaderInstance LOGO_GLITCH_TEXT_INTENSITY_POSITION_COLOR_TEX_LIGHTMAP;

    public ModShaders(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    } 

    private static ShaderInstance MODERN_GAUSSIAN_BLUR;
    private static ShaderInstance GRAY;
    private static ShaderInstance X_REVERSE;

    public static ShaderInstance getGray() {
        return GRAY;
    }

    public static void setGray(ShaderInstance GRAY) {
        ModShaders.GRAY = GRAY;
    }

    public static ShaderInstance getXReverse() {
        return X_REVERSE;
    }

    public static void setXReverse(ShaderInstance xReverse) {
        X_REVERSE = xReverse;
    }

    public static ShaderInstance getAlphaFilter() {
        return ALPHA_FILTER;
    }

    public static void setAlphaFilter(ShaderInstance alphaFilter) {
        ALPHA_FILTER = alphaFilter;
    }

    private static ShaderInstance ALPHA_FILTER;

    public static ShaderInstance getRgbOutline() {
        return RGB_OUTLINE;
    }

    public static void setRgbOutline(ShaderInstance rgbOutline) {
        RGB_OUTLINE = rgbOutline;
    }

    public static ShaderInstance getDissolve2d() {
        return DISSOLVE_2D;
    }

    public static void setDissolve2d(ShaderInstance dissolve2d) {
        DISSOLVE_2D = dissolve2d;
    }

    public static ShaderInstance getMap2Start() {
        return MAP2_START;
    }

    public static void setMap2Start(ShaderInstance map2Start) {
        MAP2_START = map2Start;
    }

    public static ShaderInstance getMapHealthBackground() {
        return MAP_HEALTH_BACKGROUND;
    }

    public static void setMapHealthBackground(ShaderInstance mapHealthBackground) {
        MAP_HEALTH_BACKGROUND = mapHealthBackground;
    }

    public static ShaderInstance getMapPositionTex() {
        return MAP_POSITION_TEX;
    }

    public static void setMapPositionTex(ShaderInstance mapPositionTex) {
        MAP_POSITION_TEX = mapPositionTex;
    }
    public static ShaderInstance getGuiBlurRect() {
        return GUI_BLUR_RECT;
    }
    public static void setGuiBlurRect(ShaderInstance guiBlurRect) {
        GUI_BLUR_RECT = guiBlurRect;
    }
    public static ShaderInstance getLogoGlitch() {
        return LOGO_GLITCH;
    }
    public static void setLogoGlitch(ShaderInstance logoGlitch) {
        LOGO_GLITCH = logoGlitch;
    }
    public static ShaderInstance getLogoGlitchPositionColorTexLightmap() {
        return LOGO_GLITCH_POSITION_COLOR_TEX_LIGHTMAP;
    }
    public static void setLogoGlitchPositionColorTexLightmap(ShaderInstance logoGlitchPositionColorTexLightmap) {
        LOGO_GLITCH_POSITION_COLOR_TEX_LIGHTMAP = logoGlitchPositionColorTexLightmap;
    }
    public static ShaderInstance getLogoGlitchTextIntensityPositionColorTexLightmap() {
        return LOGO_GLITCH_TEXT_INTENSITY_POSITION_COLOR_TEX_LIGHTMAP;
    }
    public static void setLogoGlitchTextIntensityPositionColorTexLightmap(ShaderInstance shaderInstance) {
        LOGO_GLITCH_TEXT_INTENSITY_POSITION_COLOR_TEX_LIGHTMAP = shaderInstance;
    }
    public static ShaderInstance getVoronoiFlowBackground() {
        return VORONOI_FLOW_BACKGROUND;
    }
    public static void setVoronoiFlowBackground(ShaderInstance voronoiFlowBackground) {
        VORONOI_FLOW_BACKGROUND = voronoiFlowBackground;
    }
    public static void dissolve2d(float progress) {
        RenderSystem.setShaderTexture(1, NOISE);
        ShaderInstance shader = getDissolve2d();
        shader.safeGetUniform("Dissolve").set(progress);
    }
    public static void rgbOutline(float r, float g, float b) {
        ShaderInstance shader = getRgbOutline();
        shader.safeGetUniform("OutlineColor").set(new float[] {r, g ,b});
    }
    public static void voronoiFlowInit(float screenWidth, float screenHeight, ShaderInstance shader) {
        shader.safeGetUniform("Size").set(new float[] {screenWidth, screenHeight});
    }
    public static void voronoiFlowColorA(Vector4f color, ShaderInstance shader) {
        voronoiFlowColorA(color.x, color.y, color.z, color.w, shader);
    }
    public static void voronoiFlowColorA(float r, float g, float b, float a, ShaderInstance shader) {
        shader.safeGetUniform("ColorA").set(new float[] {r, g, b, a});
    }
    public static void voronoiFlowColorB(Vector4f color, ShaderInstance shader) {
        voronoiFlowColorB(color.x, color.y, color.z, color.w, shader);
    }
    public static void voronoiFlowColorB(float r, float g, float b, float a, ShaderInstance shader) {
        shader.safeGetUniform("ColorB").set(new float[] {r, g, b, a});
    }
    public static void voronoiFlowColorC(Vector4f color, ShaderInstance shader) {
        voronoiFlowColorC(color.x, color.y, color.z, color.w, shader);
    }
    public static void voronoiFlowColorC(float r, float g, float b, float a, ShaderInstance shader) {
        shader.safeGetUniform("ColorC").set(new float[] {r, g, b, a});
    }
    public static void alphaFilterGray(float g) {
        ShaderInstance shaderInstance = getAlphaFilter();
        shaderInstance.safeGetUniform("Gray").set(g);
    }
    public static void logoGlitch(float time, float glitchStrength) {
        ShaderInstance shaderInstance = getLogoGlitch();
        logoGlitch(time, glitchStrength, shaderInstance);
    }
    public static void logoGlitchPositionColorTexLightmap(float time, float glitchStrength) {
        ShaderInstance shaderInstance = getLogoGlitchPositionColorTexLightmap();
        logoGlitch(time, glitchStrength, shaderInstance);
    }
    public static void logoGlitch(float time, float glitchStrength, ShaderInstance shaderInstance) {
        if (shaderInstance == null) {
            return;
        }
        shaderInstance.safeGetUniform("_ProgramTime").set(time);
        shaderInstance.safeGetUniform("GlitchStrength").set(glitchStrength);
    }
}
