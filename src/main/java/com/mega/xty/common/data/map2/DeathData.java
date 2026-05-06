package com.mega.xty.common.data.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.mixin.accessor.AccessorGuiGraphics;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.renderer.BlurRectRenderer;
import com.mega.xty.client.shader.ModShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class DeathData {
    public final UUID id = UUID.randomUUID();
    @NotNull
    public Component killer;
    @NotNull
    public ItemStack weapon;
    @NotNull
    public Component beKilled;
    public int tickCount;
    public DeathData(@NotNull Component killer, @NotNull ItemStack weapon, @NotNull Component beKilled) {
        this.killer = killer;
        this.weapon = weapon;
        this.beKilled = beKilled;
    }
    public void add() {
        ClientGameData.toAddDeathData.add(this);
    }
    public void tick() {
        tickCount++;
        if (this.tickCount >= 130)
            ClientGameData.toRemoveDeathData.add(this);
    }
    @OnlyIn(Dist.CLIENT)
    public Consumer<PoseStack> render(MegaGuiGraphics graphics, PoseStack poseStack, Font font, float partialTicks) {
        if (tickCount <= 120) {
            float appearProgress = Easing.OUT_CUBIC.calculate(Math.min((this.tickCount + partialTicks) / 15F, 1.0F));
            float disappearProgress = this.tickCount > 120 || this.tickCount < 105 ? 0F : Easing.IN_OUT_SINE.calculate(Math.min((tickCount - 105 + partialTicks), 15F) / 15F);
            float mixedAlpha = Mth.clamp(appearProgress * (1F - disappearProgress), 0F, 1F);
            int width = getWidth(font, weapon);
            poseStack.pushPose();
            poseStack.translate(width * (1F-appearProgress), 0, 0);
            //背景
            BlurRectRenderer.render(graphics, -4, -1, width + 5, 11.5F, ((int) (mixedAlpha * 100)) << 24 | 0x00303030, 4);
            RenderSystem.setShaderColor(1F, 1F, 1F, mixedAlpha);

            int space = graphics.drawString(font, this.killer, 0, 0, 0xFFFFFFFF);
            if (!this.weapon.isEmpty()) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                renderItemOrGun(graphics, this.weapon, space, -4);
            }
            graphics.drawString(font, this.beKilled, space + getItemDisplayWidth(weapon), 0, ((int) (mixedAlpha * 255F)) << 24 | 0x00FFFFFF);
            poseStack.popPose();
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }
        float disappearTranslationProgress = this.tickCount > 130 || this.tickCount < 120 ? 0F : Easing.IN_OUT_SINE.calculate(Math.min((tickCount - 120 + partialTicks), 10F) / 10F);
        return (ps) -> ps.translate(0, 13F * (1-disappearTranslationProgress), 0F);
    }
    public int getWidth(Font font, ItemStack itemStack) {
        return font.width(this.beKilled) + font.width(this.killer) + this.getItemDisplayWidth(itemStack);
    }
    public int getItemDisplayWidth(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        return itemStack.getItem() instanceof IGun ? 34 : 20;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeathData data = (DeathData) o;
        return Objects.equals(id, data.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    public static void renderItemOrGun(MegaGuiGraphics graphics, ItemStack itemStack, int x, int y) {
        if (itemStack.getItem() instanceof IGun) {
            Optional<GunDisplayInstance> optionalGunDisplayInstance = TimelessAPI.getGunDisplay(itemStack);
            optionalGunDisplayInstance.ifPresent(display -> {
                graphics.blit(display.getHUDTexture(), x + 2F, y + 4F, 30F, 10F, 0.0F, 0.0F, 39, 13, 39, 13, ModShaders::getXReverse);
            });
            if (optionalGunDisplayInstance.isEmpty()) {
                graphics.renderItem(itemStack, x, y);
            }
        } else {
            graphics.renderItem(itemStack, x, y);
        }
    }
}
