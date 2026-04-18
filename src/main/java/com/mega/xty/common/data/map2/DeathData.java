package com.mega.xty.common.data.map2;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.mixin.accessor.AccessorGuiGraphics;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
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
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
        if (this.tickCount > 120)
            ClientGameData.toRemoveDeathData.add(this);
    }
    @OnlyIn(Dist.CLIENT)
    public void render(MegaGuiGraphics graphics, PoseStack poseStack, Font font, float partialTicks) {
        float alpha = Easing.OUT_CUBIC.calculate(Math.min((this.tickCount + partialTicks) / 10F, 1.0F));
        int width = getWidth(font, weapon);
        RenderSystem.setShaderColor(alpha, alpha, alpha, alpha);
        poseStack.pushPose();
        poseStack.translate(width * (1F-alpha), 0, 0);
        //背景
        VertexConsumer vertexConsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        fillGradient(poseStack, vertexConsumer,  -4, -1F, width + 1, 10.5F, 0, 4, 0x7C666666, 0x10666666);
        fillGradient(poseStack, vertexConsumer, -4, -1F, width + 1, 10.5F, 0, 4, 0x453a5595, 0x453a5595);
        ((AccessorGuiGraphics) graphics).callFlushIfUnmanaged();

        int space = graphics.drawString(font, this.killer, 0, 0, 0xFFFFFFFF);
        renderItemOrGun(graphics, this.weapon, space, -4);
        graphics.drawString(font, this.beKilled, space + getItemDisplayWidth(weapon), 0, 0xFFFFFFFF);
        poseStack.popPose();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }
    public int getWidth(Font font, ItemStack itemStack) {
        return font.width(this.beKilled) + font.width(this.killer) + this.getItemDisplayWidth(itemStack);
    }
    public int getItemDisplayWidth(ItemStack itemStack) {
        return itemStack.getItem() instanceof IGun ? 34 : 20;
    }
    private void fillGradient(PoseStack poseStack, VertexConsumer vertexConsumer, float x, float y, float endx, float endy, float depth, float xOffset, int color1, int color2) {
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.vertex(matrix4f, x, y, depth).color(color1).endVertex();
        vertexConsumer.vertex(matrix4f, x + xOffset, endy, depth).color(color2).endVertex();
        vertexConsumer.vertex(matrix4f, endx, endy, depth).color(color2).endVertex();
        vertexConsumer.vertex(matrix4f, endx, y, depth).color(color1).endVertex();
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
