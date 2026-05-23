package com.mega.map.client.font;

import com.google.common.collect.Lists;
import com.mega.endinglib.mixin.accessor.AccessorFont;
import com.mega.endinglib.mixin.accessor.AccessorFontManager;
import com.mega.endinglib.mixin.accessor.AccessorMC;
import com.mega.endinglib.util.time.TimeContext;
import com.mega.map.client.shader.ModShaders;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mega.map.mixin.client.AccessorEmptyTextureStateShard;
import com.mega.map.mixin.client.AccessorRenderTypeCompositeRenderType;
import com.mega.map.mixin.client.AccessorRenderTypeCompositeState;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ErrorFont extends Font {
    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
    public static FontManager fontManager = ((AccessorMC) Minecraft.getInstance()).getFontManager();
    public static AccessorFontManager accessorFontManager = (AccessorFontManager) fontManager;
    public static ErrorFont INSTANCE;

    static {
        INSTANCE = new ErrorFont((p_284586_) -> accessorFontManager.getFontSets().getOrDefault(accessorFontManager.getRenames().getOrDefault(p_284586_, p_284586_), accessorFontManager.getMissingFontSet()), false, accessorFontManager.getFontSets());
    }

    public final AccessorFont accessorFont;

    public ErrorFont(Function<ResourceLocation, FontSet> p_243253_, boolean p_243245_, Map<ResourceLocation, FontSet> f) {
        super(p_243253_, p_243245_);
        this.accessorFont = (AccessorFont) this;
    }

    @Override
    public int drawInBatch(@NotNull FormattedCharSequence p_273262_, float p_273006_, float p_273254_, int p_273375_, boolean p_273674_, @NotNull Matrix4f p_273525_, @NotNull MultiBufferSource p_272624_, @NotNull DisplayMode p_273418_, int p_273330_, int p_272981_) {
        return this.drawInternal(p_273262_, p_273006_, p_273254_, p_273375_, p_273674_, p_273525_, p_272624_, p_273418_, p_273330_, p_272981_);
    }

    @Override
    public int drawInBatch(String p_272751_, float p_272661_, float p_273129_, int p_273272_, boolean p_273209_, Matrix4f p_272940_, MultiBufferSource p_273017_, DisplayMode p_272608_, int p_273365_, int p_272755_) {
        return this.drawInBatch(p_272751_, p_272661_, p_273129_, p_273272_, p_273209_, p_272940_, p_273017_, p_272608_, p_273365_, p_272755_, this.isBidirectional());
    }

    @Override
    public int drawInBatch(Component p_273032_, float p_273249_, float p_273594_, int p_273714_, boolean p_273050_, Matrix4f p_272974_, MultiBufferSource p_273695_, DisplayMode p_272782_, int p_272603_, int p_273632_) {
        return this.drawInBatch(p_273032_.getVisualOrderText(), p_273249_, p_273594_, p_273714_, p_273050_, p_272974_, p_273695_, p_272782_, p_272603_, p_273632_);
    }

    @Override
    public int drawInBatch(String p_272780_, float p_272811_, float p_272610_, int p_273422_, boolean p_273016_, Matrix4f p_273443_, MultiBufferSource p_273387_, DisplayMode p_273551_, int p_272706_, int p_273114_, boolean p_273022_) {
        return this.drawInternal(p_272780_, p_272811_, p_272610_, p_273422_, p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
    }
    private int drawInternal(String p_273658_, float p_273086_, float p_272883_, int p_273547_, boolean p_272778_, Matrix4f p_272662_, MultiBufferSource p_273012_, Font.DisplayMode p_273381_, int p_272855_, int p_272745_, boolean p_272785_) {
        if (p_272785_) {
            p_273658_ = this.bidirectionalShaping(p_273658_);
        }

        p_273547_ = adjustColor(p_273547_);
        Matrix4f matrix4f = new Matrix4f(p_272662_);
        if (p_272778_) {
            this.renderText(p_273658_, p_273086_, p_272883_, p_273547_, true, p_272662_, p_273012_, p_273381_, p_272855_, p_272745_);
            matrix4f.translate(SHADOW_OFFSET);
        }

        p_273086_ = this.renderText(p_273658_, p_273086_, p_272883_, p_273547_, false, matrix4f, p_273012_, p_273381_, p_272855_, p_272745_);
        return (int)p_273086_ + (p_272778_ ? 1 : 0);
    }
    private int drawInternal(FormattedCharSequence p_273025_, float p_273121_, float p_272717_, int p_273653_, boolean p_273531_, Matrix4f p_273265_, MultiBufferSource p_273560_, Font.DisplayMode p_273342_, int p_273373_, int p_273266_) {
        p_273653_ = adjustColor(p_273653_);
        Matrix4f matrix4f = new Matrix4f(p_273265_);
        if (p_273531_) {
            this.renderText(p_273025_, p_273121_, p_272717_, p_273653_, true, p_273265_, p_273560_, p_273342_, p_273373_, p_273266_);
            matrix4f.translate(SHADOW_OFFSET);
        }

        p_273121_ = this.renderText(p_273025_, p_273121_, p_272717_, p_273653_, false, matrix4f, p_273560_, p_273342_, p_273373_, p_273266_);
        return (int)p_273121_ + (p_273531_ ? 1 : 0);
    }
    private float renderText(String p_273765_, float p_273532_, float p_272783_, int p_273217_, boolean p_273583_, Matrix4f p_272734_, MultiBufferSource p_272595_, Font.DisplayMode p_273610_, int p_273727_, int p_273199_) {
        applyLogoGlitchUniforms();
        StringRenderOutputError font$stringrenderoutput = new StringRenderOutputError(p_272595_, p_273532_, p_272783_, p_273217_, p_273583_, p_272734_, p_273610_, p_273199_);
        StringDecomposer.iterateFormatted(p_273765_, Style.EMPTY, font$stringrenderoutput);
        return font$stringrenderoutput.finish(p_273727_, p_273532_);
    }
    private float renderText(FormattedCharSequence p_273322_, float p_272632_, float p_273541_, int p_273200_, boolean p_273312_, Matrix4f p_273276_, MultiBufferSource p_273392_, Font.DisplayMode p_272625_, int p_273774_, int p_273371_) {
        applyLogoGlitchUniforms();
        StringRenderOutputError font$stringrenderoutput = new StringRenderOutputError(p_273392_, p_272632_, p_273541_, p_273200_, p_273312_, p_273276_, p_272625_, p_273371_);
        p_273322_.accept(font$stringrenderoutput);
        return font$stringrenderoutput.finish(p_273774_, p_272632_);
    }
    private static int adjustColor(int p_92720_) {
        return (p_92720_ & -67108864) == 0 ? p_92720_ | -16777216 : p_92720_;
    }

    private static void applyLogoGlitchUniforms() {
        float time = TimeContext.Client.currentSeconds() * 16.0F;
        ModShaders.logoGlitch(time, 1.5F, ModShaders.getLogoGlitchPositionColorTexLightmap());
        ModShaders.logoGlitch(time, 1.5F, ModShaders.getLogoGlitchTextIntensityPositionColorTexLightmap());
    }

    private static RenderType getErrorFontRenderType(BakedGlyph bakedGlyph, DisplayMode mode) {
        RenderType originalRenderType = bakedGlyph.renderType(mode);
        ResourceLocation texture = getRenderTypeTexture(originalRenderType);
        if (texture == null) {
            return originalRenderType;
        }

        boolean intensity = isIntensityRenderType(originalRenderType, texture, mode);
        if (intensity) {
            return ModShaders.getLogoGlitchTextIntensityPositionColorTexLightmap() == null
                    ? originalRenderType
                    : ErrorFontRenderTypes.get(texture, mode, true);
        }
        return ModShaders.getLogoGlitchPositionColorTexLightmap() == null
                ? originalRenderType
                : ErrorFontRenderTypes.get(texture, mode, false);
    }

    @Nullable
    private static ResourceLocation getRenderTypeTexture(RenderType renderType) {
        if (!(renderType instanceof AccessorRenderTypeCompositeRenderType accessorRenderType)) {
            return null;
        }

        RenderType.CompositeState state = accessorRenderType.getState();
        RenderStateShard.EmptyTextureStateShard textureState = ((AccessorRenderTypeCompositeState) (Object) state).getTextureState();
        Optional<ResourceLocation> texture = ((AccessorEmptyTextureStateShard) (Object) textureState).invokeCutoutTexture();
        return texture.orElse(null);
    }

    private static boolean isIntensityRenderType(RenderType originalRenderType, ResourceLocation texture, DisplayMode mode) {
        return switch (mode) {
            case NORMAL -> originalRenderType == RenderType.textIntensity(texture);
            case SEE_THROUGH -> originalRenderType == RenderType.textIntensitySeeThrough(texture);
            case POLYGON_OFFSET -> originalRenderType == RenderType.textIntensityPolygonOffset(texture);
        };
    }

    private static final class ErrorFontRenderTypes extends RenderType {
        private static final ShaderStateShard LOGO_GLITCH_TEXT_SHADER = new ShaderStateShard(ModShaders::getLogoGlitchPositionColorTexLightmap);
        private static final ShaderStateShard LOGO_GLITCH_TEXT_INTENSITY_SHADER = new ShaderStateShard(ModShaders::getLogoGlitchTextIntensityPositionColorTexLightmap);
        private static final Function<ResourceLocation, RenderType> TEXT = Util.memoize(texture ->
                create("megamod:error_font_text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                        CompositeState.builder()
                                .setShaderState(LOGO_GLITCH_TEXT_SHADER)
                                .setTextureState(new TextureStateShard(texture, false, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setLightmapState(LIGHTMAP)
                                .createCompositeState(false)));
        private static final Function<ResourceLocation, RenderType> TEXT_SEE_THROUGH = Util.memoize(texture ->
                create("megamod:error_font_text_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                        CompositeState.builder()
                                .setShaderState(LOGO_GLITCH_TEXT_SHADER)
                                .setTextureState(new TextureStateShard(texture, false, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setLightmapState(LIGHTMAP)
                                .setDepthTestState(NO_DEPTH_TEST)
                                .setWriteMaskState(COLOR_WRITE)
                                .createCompositeState(false)));
        private static final Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET = Util.memoize(texture ->
                create("megamod:error_font_text_polygon_offset", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                        CompositeState.builder()
                                .setShaderState(LOGO_GLITCH_TEXT_SHADER)
                                .setTextureState(new TextureStateShard(texture, false, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setLightmapState(LIGHTMAP)
                                .setLayeringState(POLYGON_OFFSET_LAYERING)
                                .createCompositeState(false)));
        private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY = Util.memoize(texture ->
                create("megamod:error_font_text_intensity", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                        CompositeState.builder()
                                .setShaderState(LOGO_GLITCH_TEXT_INTENSITY_SHADER)
                                .setTextureState(new TextureStateShard(texture, false, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setLightmapState(LIGHTMAP)
                                .createCompositeState(false)));
        private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_SEE_THROUGH = Util.memoize(texture ->
                create("megamod:error_font_text_intensity_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                        CompositeState.builder()
                                .setShaderState(LOGO_GLITCH_TEXT_INTENSITY_SHADER)
                                .setTextureState(new TextureStateShard(texture, false, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setLightmapState(LIGHTMAP)
                                .setDepthTestState(NO_DEPTH_TEST)
                                .setWriteMaskState(COLOR_WRITE)
                                .createCompositeState(false)));
        private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize(texture ->
                create("megamod:error_font_text_intensity_polygon_offset", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
                        CompositeState.builder()
                                .setShaderState(LOGO_GLITCH_TEXT_INTENSITY_SHADER)
                                .setTextureState(new TextureStateShard(texture, false, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setLightmapState(LIGHTMAP)
                                .setLayeringState(POLYGON_OFFSET_LAYERING)
                                .createCompositeState(false)));

        private ErrorFontRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        private static RenderType get(ResourceLocation texture, DisplayMode mode, boolean intensity) {
            return switch (mode) {
                case NORMAL -> intensity ? TEXT_INTENSITY.apply(texture) : TEXT.apply(texture);
                case SEE_THROUGH -> intensity ? TEXT_INTENSITY_SEE_THROUGH.apply(texture) : TEXT_SEE_THROUGH.apply(texture);
                case POLYGON_OFFSET -> intensity ? TEXT_INTENSITY_POLYGON_OFFSET.apply(texture) : TEXT_POLYGON_OFFSET.apply(texture);
            };
        }
    }

    class StringRenderOutputError extends StringRenderOutput {
        final MultiBufferSource bufferSource;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f pose;
        private final DisplayMode mode;
        private final int packedLightCoords;
        float x;
        float y;
        @Nullable
        private List<net.minecraft.client.gui.font.glyphs.BakedGlyph.Effect> effects;

        public StringRenderOutputError(MultiBufferSource p_181365_, float p_181366_, float p_181367_, int p_181368_, boolean p_181369_, Matrix4f p_254510_, DisplayMode p_181371_, int p_181372_) {
            super(p_181365_, p_181366_, p_181367_, p_181368_, p_181369_, p_254510_, p_181371_, p_181372_);
            this.bufferSource = p_181365_;
            this.x = p_181366_;
            this.y = p_181367_;
            this.dropShadow = p_181369_;
            this.dimFactor = p_181369_ ? 0.25F : 1.0F;
            this.r = (float) (p_181368_ >> 16 & 255) / 255.0F * this.dimFactor;
            this.g = (float) (p_181368_ >> 8 & 255) / 255.0F * this.dimFactor;
            this.b = (float) (p_181368_ & 255) / 255.0F * this.dimFactor;
            this.a = (float) (p_181368_ >> 24 & 255) / 255.0F;
            this.pose = p_254510_;
            this.mode = p_181371_;
            this.packedLightCoords = p_181372_;
        }

        public void addEffect(net.minecraft.client.gui.font.glyphs.BakedGlyph.@NotNull Effect p_92965_) {
            if (this.effects == null) {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(p_92965_);
        }

        public boolean accept(int p_92967_, Style style, int p_92969_) {
            FontSet fontset = ErrorFont.this.accessorFont.invokeGetFontSet(style.getFont());
            GlyphInfo glyphinfo = fontset.getGlyphInfo(p_92969_, ErrorFont.this.accessorFont.isFilterFishyGlyphs());
            net.minecraft.client.gui.font.glyphs.BakedGlyph bakedglyph = style.isObfuscated() && p_92969_ != 32 ? fontset.getRandomGlyph(glyphinfo) : fontset.getGlyph(p_92969_);
            boolean flag = style.isBold();
            float f3 = this.a;
            TextColor textcolor = style.getColor();
            float f;
            float f1;
            float f2;
            if (textcolor != null) {
                int i = textcolor.getValue();
                f = (float) (i >> 16 & 255) / 255.0F * this.dimFactor;
                f1 = (float) (i >> 8 & 255) / 255.0F * this.dimFactor;
                f2 = (float) (i & 255) / 255.0F * this.dimFactor;
            } else {
                f = this.r;
                f1 = this.g;
                f2 = this.b;
            }
            if (!(bakedglyph instanceof EmptyGlyph)) {
                float f5 = flag ? glyphinfo.getBoldOffset() : 0.0F;
                float f4 = this.dropShadow ? glyphinfo.getShadowOffset() : 0.0F;
                VertexConsumer vertexconsumer = this.bufferSource.getBuffer(getErrorFontRenderType(bakedglyph, this.mode));
                ErrorFont.this.accessorFont.callRenderChar(bakedglyph, flag, style.isItalic(), f5, this.x + f4, this.y + f4, this.pose, vertexconsumer, f, f1, f2, f3, this.packedLightCoords);
            }

            float f6 = glyphinfo.getAdvance(flag);
            float f7 = this.dropShadow ? 1.0F : 0.0F;
            if (style.isStrikethrough()) {
                this.addEffect(new net.minecraft.client.gui.font.glyphs.BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, f3));
            }

            if (style.isUnderlined()) {
                this.addEffect(new net.minecraft.client.gui.font.glyphs.BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f, f1, f2, f3));
            }

            this.x += f6;
            return true;
        }

        public float finish(int p_92962_, float p_92963_) {
            if (p_92962_ != 0) {
                float f = (float) (p_92962_ >> 24 & 255) / 255.0F;
                float f1 = (float) (p_92962_ >> 16 & 255) / 255.0F;
                float f2 = (float) (p_92962_ >> 8 & 255) / 255.0F;
                float f3 = (float) (p_92962_ & 255) / 255.0F;
                this.addEffect(new net.minecraft.client.gui.font.glyphs.BakedGlyph.Effect(p_92963_ - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (this.effects != null) {
                net.minecraft.client.gui.font.glyphs.BakedGlyph bakedglyph = ErrorFont.this.accessorFont.invokeGetFontSet(Style.DEFAULT_FONT).whiteGlyph();
                VertexConsumer vertexconsumer = this.bufferSource.getBuffer(getErrorFontRenderType(bakedglyph, this.mode));

                for (net.minecraft.client.gui.font.glyphs.BakedGlyph.Effect bakedglyph$effect : this.effects) {
                    bakedglyph.renderEffect(bakedglyph$effect, this.pose, vertexconsumer, this.packedLightCoords);
                }
            }

            return this.x;
        }
    }
}
