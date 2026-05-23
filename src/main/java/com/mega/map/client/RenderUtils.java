package com.mega.map.client;

import com.mega.map.MegaMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;

public class RenderUtils extends RenderType {
    protected static ShaderInstance LOADING_OVERLAY_SHADER;
    protected static final RenderStateShard.ShaderStateShard LOADING_OVERLAY_STATE = new RenderStateShard.ShaderStateShard(RenderUtils::getLoadingOverlayShader);
    @Nullable
    private static RenderType LOADING_OVERLAY_RENDER_TYPE;

    public RenderUtils(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    public static ShaderInstance getLoadingOverlayShader() {
        return LOADING_OVERLAY_SHADER;
    }
    @Nullable
    public static RenderType loadingOverlay() {
        return LOADING_OVERLAY_RENDER_TYPE;
    }
    public static void preloadRenderTypes() {
        RenderUtils.LOADING_OVERLAY_RENDER_TYPE = create(MegaMod.MODID + ":loading_overlay", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(LOADING_OVERLAY_STATE).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    }

    public static void setLoadingOverlayShader(ShaderInstance loadingOverlay) {
        LOADING_OVERLAY_SHADER = loadingOverlay;
    }
}
