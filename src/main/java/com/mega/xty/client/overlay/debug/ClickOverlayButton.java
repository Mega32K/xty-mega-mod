package com.mega.xty.client.overlay.debug;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClickOverlayButton implements IGuiOverlay {
    private float buttonAlphaOld = 0.0F;
    private float buttonAlpha = 0.0F;
    public MutableComponent text;
    public MutableComponent textStrikethrough;
    public Consumer<Player> consumer = (player -> {});
    public BiFunction<ItemStack, Player, Boolean> canUse = ((stack, player) -> true);
    public ClickOverlayButton(MutableComponent text) {
        setText(text);
    }
    private boolean canUseNow = true;
    public ClickOverlayButton setConsumer(Consumer<Player> consumer) {
        this.consumer = consumer;
        return this;
    }

    public ClickOverlayButton setCanUse(BiFunction<ItemStack, Player, Boolean> canUse) {
        this.canUse = canUse;
        return this;
    }

    public void setText(MutableComponent text) {
        this.text = text;
        this.textStrikethrough = this.text.copy().withStyle(ChatFormatting.STRIKETHROUGH);
    }

    public void click(Player player) {
        if (canUse.apply(player.getItemInHand(InteractionHand.MAIN_HAND), player)) {
            buttonAlpha = 1F;
            consumer.accept(player);
        }
    }

    public void tick() {
        buttonAlphaOld = buttonAlpha;
        if (buttonAlpha > 0.0F)
            buttonAlpha = Math.max(0F, buttonAlpha - 0.2F);
        Player player = ClientWrapped.clientPlayer();
        if (player != null)
            canUseNow = canUse.apply(player.getItemInHand(InteractionHand.MAIN_HAND), player);

    }

    public float getAlpha(float partialTicks) {
        return Mth.lerp(partialTicks, buttonAlphaOld, buttonAlpha);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
        Font font = gui.getFont();
        float alpha = this.getAlpha(partialTick);

        float sizeOfText = font.width(this.text);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate((Easing.IN_OUT_ELASTIC.calculate(alpha) - 1F) * 1.8F, 0F, 0F);
        graphics.fill(0, 0, sizeOfText + 4, font.lineHeight + 3.5F, FastColor.ARGB32.color((int) (alpha * 255), 39, 48, 63));
        graphics.drawString(font, canUseNow ? text : textStrikethrough, 2, 2, 0xFFFFFFFF, true);
        poseStack.popPose();
    }
}
