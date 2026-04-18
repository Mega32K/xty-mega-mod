package com.mega.xty.client.shader;

import com.mega.xty.XtyMegaMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector4f;

public class ModShaders {
    public static final ResourceLocation NOISE = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/noise.png");
    private static ShaderInstance MAP_POSITION_TEX;
    private static ShaderInstance MAP_HEALTH_BACKGROUND;
    private static ShaderInstance MAP2_START;
    private static ShaderInstance DISSOLVE_2D;
    private static ShaderInstance RGB_OUTLINE;
    private static ShaderInstance VORONOI_FLOW_BACKGROUND;

    public static ShaderInstance getGray() {
        return GRAY;
    }

    public static void setGray(ShaderInstance GRAY) {
        ModShaders.GRAY = GRAY;
    }

    private static ShaderInstance GRAY;
    public static ShaderInstance getXReverse() {
        return X_REVERSE;
    }

    public static void setXReverse(ShaderInstance xReverse) {
        X_REVERSE = xReverse;
    }

    private static ShaderInstance X_REVERSE;

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
}
