package com.mega.xty.client.renderer;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.shader.ModShaders;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class BlurRectRenderer {
    private static final float MAX_BLUR_RADIUS = 32.0F;
    public static TextureTarget screenCopy;

    private BlurRectRenderer() {
    }

    public static void render(GuiGraphics graphics, float x, float y, float width, float height, int color, float blurRadius) {
        render(MegaGuiGraphics.of(graphics), x, y, width, height, color, blurRadius);
    }

    public static void render(MegaGuiGraphics graphics, float x, float y, float width, float height, int color, float blurRadius) {
        if (width <= 0.0F || height <= 0.0F) {
            return;
        }

        ShaderInstance shader = ModShaders.getGuiBlurRect();
        if (shader == null) {
            graphics.fill(x, y, x + width, y + height, color);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        ensureScreenCopy(mainTarget);

        //graphics.flush();
        copyMainTarget(mainTarget);
        mainTarget.bindWrite(false);

        float guiWidth = (float) graphics.guiWidth();
        float guiHeight = (float) graphics.guiHeight();
        float scaleX = (float) mainTarget.viewWidth / guiWidth;
        float scaleY = (float) mainTarget.viewHeight / guiHeight;
        Matrix4f matrix4f = graphics.pose().last().pose();
        Vector4f topLeft = transformCorner(matrix4f, x, y, 0.0F);
        Vector4f bottomLeft = transformCorner(matrix4f, x, y + height, 0.0F);
        Vector4f bottomRight = transformCorner(matrix4f, x + width, y + height, 0.0F);
        Vector4f topRight = transformCorner(matrix4f, x + width, y, 0.0F);

        float topLeftU = topLeft.x() * scaleX / (float) mainTarget.width;
        float bottomLeftU = bottomLeft.x() * scaleX / (float) mainTarget.width;
        float bottomRightU = bottomRight.x() * scaleX / (float) mainTarget.width;
        float topRightU = topRight.x() * scaleX / (float) mainTarget.width;
        float topLeftV = 1.0F - topLeft.y() * scaleY / (float) mainTarget.height;
        float bottomLeftV = 1.0F - bottomLeft.y() * scaleY / (float) mainTarget.height;
        float bottomRightV = 1.0F - bottomRight.y() * scaleY / (float) mainTarget.height;
        float topRightV = 1.0F - topRight.y() * scaleY / (float) mainTarget.height;

        float rectMinU = Math.min(Math.min(topLeftU, bottomLeftU), Math.min(bottomRightU, topRightU));
        float rectMaxU = Math.max(Math.max(topLeftU, bottomLeftU), Math.max(bottomRightU, topRightU));
        float rectMinV = Math.min(Math.min(topLeftV, bottomLeftV), Math.min(bottomRightV, topRightV));
        float rectMaxV = Math.max(Math.max(topLeftV, bottomLeftV), Math.max(bottomRightV, topRightV));

        shader.safeGetUniform("TextureSize").set(new float[]{mainTarget.width, mainTarget.height});
        shader.safeGetUniform("RectMin").set(new float[]{rectMinU, rectMinV});
        shader.safeGetUniform("RectMax").set(new float[]{rectMaxU, rectMaxV});
        shader.safeGetUniform("RectColor").set(new float[]{
                (float) FastColor.ARGB32.red(color) / 255.0F,
                (float) FastColor.ARGB32.green(color) / 255.0F,
                (float) FastColor.ARGB32.blue(color) / 255.0F,
                (float) FastColor.ARGB32.alpha(color) / 255.0F
        });
        shader.safeGetUniform("BlurRadius").set(Mth.clamp(blurRadius, 0.0F, MAX_BLUR_RADIUS));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            RenderSystem.setShader(ModShaders::getGuiBlurRect);
            RenderSystem.setShaderTexture(0, screenCopy.getColorTextureId());

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix4f, x, y, 0.0F).uv(topLeftU, topLeftV).endVertex();
            bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).uv(bottomLeftU, bottomLeftV).endVertex();
            bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).uv(bottomRightU, bottomRightV).endVertex();
            bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).uv(topRightU, topRightV).endVertex();
            BufferUploader.drawWithShader(bufferBuilder.end());
        } finally {
            RenderSystem.disableBlend();
        }
    }

    public static void ensureScreenCopy(RenderTarget mainTarget) {
        if (screenCopy != null && screenCopy.width == mainTarget.width && screenCopy.height == mainTarget.height) {
            return;
        }

        if (screenCopy != null) {
            screenCopy.destroyBuffers();
        }

        screenCopy = new TextureTarget(mainTarget.width, mainTarget.height, false, Minecraft.ON_OSX);
    }

    public static void copyMainTarget(RenderTarget mainTarget) {
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, screenCopy.frameBufferId);
        GlStateManager._glBlitFrameBuffer(
                0,
                0,
                mainTarget.width,
                mainTarget.height,
                0,
                0,
                screenCopy.width,
                screenCopy.height,
                GL11.GL_COLOR_BUFFER_BIT,
                GL11.GL_NEAREST
        );
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, mainTarget.frameBufferId);
    }

    public static TextureTarget getScreenCopy() {
        return screenCopy;
    }

    private static Vector4f transformCorner(Matrix4f matrix4f, float x, float y, float z) {
        Vector4f vector4f = new Vector4f(x, y, z, 1.0F).mul(matrix4f);
        if (vector4f.w() != 0.0F && vector4f.w() != 1.0F) {
            vector4f.div(vector4f.w());
        }
        return vector4f;
    }
}
