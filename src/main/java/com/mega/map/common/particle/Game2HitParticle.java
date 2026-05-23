package com.mega.map.common.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class Game2HitParticle extends TextureSheetParticle {
    public static ParticleRenderType PARTICLE_LIGHT = new ParticleRenderType() {
        public void begin(BufferBuilder bufferBuilder, @NotNull TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
        }

        public String toString() {
            return "PARTICLE_LIGHT";
        }
    };
    private final SpriteSet sprites;
    public Game2HitParticle(ClientLevel level, double x, double y, double z, double dX, double dY, double dZ, SpriteSet sprites, float roll) {
        super(level, x, y, z, dX, dY, dZ);
        this.gravity = 0.008F;
        this.lifetime = 4;
        this.xd = dX;
        this.yd = dY;
        this.zd = dZ;
        this.sprites = sprites;
        this.pickSprite(sprites);
        this.roll = roll;
        this.scale((float) (Math.random() * 0.7F + 0.7F));
        if (Math.random() < 0.3)
            age++;
        if (Math.random() < 0.3)
            age++;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            if (this.xd > 0)
                this.xd -= this.gravity;
            else this.xd += this.gravity;
            if (this.zd > 0)
                this.zd -= this.gravity;
            else this.zd += this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public void render(VertexConsumer p_107678_, Camera p_107679_, float p_107680_) {
        this.oRoll = this.roll;
        super.render(p_107678_, p_107679_, p_107680_);
    }

    @Override
    protected int getLightColor(float p_107249_) {
        return 15728880;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return PARTICLE_LIGHT;
    }
    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<Game2HitParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet p_105836_) {
            this.sprites = p_105836_;
        }

        @Nullable
        @Override
        public Particle createParticle(Game2HitParticleOption opt, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new Game2HitParticle(level, x, y, z, xd, yd, zd, this.sprites, opt.zRoll);
        }
    }
}
