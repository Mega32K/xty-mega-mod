package com.mega.map.client.overlay.fps;

import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.map.MegaMod;
import com.mega.map.common.data.map2.ClientGameData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

import java.util.Optional;

public class HotbarOverlay implements IGuiOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, "fps/hotbar");
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_SPACING = 4;
    private static final int ITEM_OFFSET = 2;
    private static final int SLOT_COUNT = 9;
    private static final int RIGHT_MARGIN = 4;
    private static final int BOTTOM_MARGIN = 4;
    private static final float GUN_ICON_WIDTH = 30.0F;
    private static final float GUN_ICON_HEIGHT = 10.0F;
    private static final float GUN_ICON_TEXTURE_WIDTH = 39.0F;
    private static final float GUN_ICON_TEXTURE_HEIGHT = 13.0F;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!shouldReplaceVanillaHotbar()) return;
        if (ClientUtils.disabledOverlays.contains(VanillaGuiOverlay.HOTBAR.type())) return;

        Minecraft mc = gui.getMinecraft();
        Player player = getHotbarPlayer(mc);
        if (player == null) return;

        gui.setupOverlayRenderState(true, false);
        MegaGuiGraphics megaGuiGraphics = MegaGuiGraphics.of(graphics);
        PoseStack poseStack = megaGuiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, -80.0F);
        renderHotbar(mc, megaGuiGraphics, player, partialTick, screenWidth, screenHeight);
        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    public static boolean shouldReplaceVanillaHotbar() {
        if (!ClientGameData.map2Playing()) return false;
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null
                && mc.gameMode != null
                && !mc.options.hideGui
                && mc.gameMode.getPlayerMode() != GameType.SPECTATOR;
    }

    private static Player getHotbarPlayer(Minecraft mc) {
        if (mc.getCameraEntity() instanceof Player player) {
            return player;
        }
        return mc.player;
    }

    private static void renderHotbar(Minecraft mc, MegaGuiGraphics graphics, Player player, float partialTick, int screenWidth, int screenHeight) {
        Font font = mc.font;
        int step = SLOT_SIZE + SLOT_SPACING;
        int rightSlotX = screenWidth - RIGHT_MARGIN - SLOT_SIZE;
        int bottomRowY = screenHeight - BOTTOM_MARGIN - SLOT_SIZE;

        for (int slot = 0; slot < 3; slot++) {
            int slotY = bottomRowY - (3 - slot) * step;
            boolean selected = slot == player.getInventory().selected;
            renderSlotNumber(graphics, font, slot + 1, rightSlotX, slotY, true);
            renderPrimarySlotItem(graphics, font, player, player.getInventory().items.get(slot), rightSlotX, slotY, partialTick, slot + 1, selected);
        }

        for (int slot = 3; slot < SLOT_COUNT; slot++) {
            boolean selected = slot == player.getInventory().selected;
            if (player.getInventory().items.get(slot).isEmpty()) {
                continue;
            }
            int slotX = rightSlotX - (slot - 3) * step;
            renderSlotNumber(graphics, font, slot + 1, slotX, bottomRowY, true);
            renderSlotItem(graphics, font, player, player.getInventory().items.get(slot), slotX + ITEM_OFFSET, bottomRowY + ITEM_OFFSET, partialTick, slot + 1, true);
        }
    }

    private static void renderSlotNumber(GuiGraphics graphics, Font font, int number, int x, int y, boolean selected) {
        String text = Integer.toString(number);
        int color = selected ? 0xFFFFFFFF : 0xFF6F737A;
        graphics.drawString(font, text, x + SLOT_SIZE - font.width(text), y - font.lineHeight + 1, color, true);
    }

    private static void renderPrimarySlotItem(MegaGuiGraphics graphics, Font font, Player player, ItemStack stack, int slotX, int slotY, float partialTick, int seed, boolean selected) {
        if (stack.isEmpty()) return;
        if (stack.getItem() instanceof IGun) {
            renderSlotItem(graphics, font, player, stack, slotX + ITEM_OFFSET, slotY + ITEM_OFFSET, partialTick, seed, selected);
            return;
        }

        String numberText = Integer.toString(seed);
        int numberX = slotX + SLOT_SIZE - font.width(numberText);
        int numberY = slotY - font.lineHeight + 1;
        int itemX = numberX - SLOT_SPACING - 16;
        int itemY = numberY - (16 - font.lineHeight) / 2;
        renderItemStack(graphics, font, player, stack, itemX, itemY, partialTick, seed);
    }

    private static void renderSlotItem(MegaGuiGraphics graphics, Font font, Player player, ItemStack stack, int x, int y, float partialTick, int seed, boolean selected) {
        if (stack.isEmpty()) return;
        if (renderGunIconLeft(graphics, stack, x - 18F - ITEM_OFFSET - 9F, y - 7F, selected)) return;
        renderItemStack(graphics, font, player, stack, x, y, partialTick, seed);
    }

    private static void renderItemStack(MegaGuiGraphics graphics, Font font, Player player, ItemStack stack, int x, int y, float partialTick, int seed) {
        float popTime = stack.getPopTime() - partialTick;
        if (popTime > 0.0F) {
            float scale = 1.0F + popTime / 5.0F;
            graphics.pose().pushPose();
            graphics.pose().translate(x + 8.0F, y + 12.0F, 0.0F);
            graphics.pose().scale(1.0F / scale, (scale + 1.0F) / 2.0F, 1.0F);
            graphics.pose().translate(-(x + 8.0F), -(y + 12.0F), 0.0F);
        }

        graphics.renderItem(player, stack, x, y, seed);
        if (popTime > 0.0F) {
            graphics.pose().popPose();
        }
        graphics.renderItemDecorations(font, stack, x, y);
    }

    private static boolean renderGunIconLeft(MegaGuiGraphics graphics, ItemStack stack, float anchorX, float y, boolean selected) {
        if (!(stack.getItem() instanceof IGun)) {
            return false;
        }

        Optional<GunDisplayInstance> optionalGunDisplayInstance = TimelessAPI.getGunDisplay(stack);
        if (optionalGunDisplayInstance.isEmpty()) {
            return false;
        }

        GunDisplayInstance display = optionalGunDisplayInstance.get();
        float color = selected ? 1.0F : 0.6F;
        graphics.flush();
        RenderSystem.setShaderColor(color, color, color, 1.0F);

        graphics.blit(display.getHUDTexture(), anchorX, y, 0.0F, 0.0F, 39, 13, 39, 13);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }

}
