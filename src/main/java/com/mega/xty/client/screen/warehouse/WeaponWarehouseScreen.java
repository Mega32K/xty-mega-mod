package com.mega.xty.client.screen.warehouse;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.c2s.warehouse.C2SSaveWeaponWarehousePacket;
import com.mega.xty.common.warehouse.WeaponWarehouseItems;
import com.mega.xty.common.warehouse.WeaponWarehouseSnapshot;
import com.mega.xty.common.warehouse.WeaponWarehouseSlotType;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WeaponWarehouseScreen extends Screen {
    private static final ResourceLocation GUI_PANEL = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/fps/gui_icons.png");
    private static final ResourceLocation GUI_PANEL_HIGHLIGHT = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/fps/gui_icons_highlight.png");
    private static final ResourceLocation SLIDER = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/fps/slider.png");
    private static final ResourceLocation SLIDER_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/fps/slider_highlighted.png");
    private static final ResourceLocation SLIDER_HANDLE = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/fps/slider_handle.png");
    private static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/fps/slider_handle_highlighted.png");
    private static final int PANEL_TEXTURE_SIZE = 96;
    private static final int PANEL_SLICE = 5;
    private static final int CANDIDATE_ITEM_HEIGHT = 20;
    private static final int SEARCH_FIELD_HEIGHT = 24;
    private static final int SCROLL_BAR_WIDTH = 8;
    private static final int SCROLL_BAR_HIT_WIDTH = 12;
    private static final int SCROLL_BAR_TEXTURE_WIDTH = 200;
    private static final int SCROLL_BAR_TEXTURE_HEIGHT = 20;
    private static final int SCROLL_BAR_TEXTURE_U = (SCROLL_BAR_TEXTURE_WIDTH - SCROLL_BAR_WIDTH) / 2;
    private static final int SCROLL_BAR_SLICE = 4;
    private static final int SCROLL_HANDLE_TEXTURE_HEIGHT = 20;
    private static final int CANDIDATE_SCROLL_GUTTER = 18;
    private static final Component SEARCH_PLACEHOLDER = Component.literal("输入要查找的武器名");

    private WeaponWarehouseSnapshot snapshot;
    private List<ItemStack> visibleCandidates = List.of();
    private WarehouseSearchBox searchBox;
    private int selectedLoadout;
    private WeaponWarehouseSlotType selectedSlot = WeaponWarehouseSlotType.MAIN_WEAPON;
    private int candidateScroll;
    private boolean draggingScrollBar;
    private float scrollBarGrabOffset;
    private WeaponWarehouseButton applyButton;
    private WeaponWarehouseButton saveButton;
    private final List<WeaponWarehouseButton> loadoutButtons = new ArrayList<>();
    private static WeaponWarehouseSnapshot latestSnapshot = WeaponWarehouseItems.createDefaultSnapshot();

    public WeaponWarehouseScreen() {
        super(Component.translatable("screen.xtymegamod.weapon_warehouse.title"));
        this.snapshot = CommonProxy.getWeaponWarehouseCap(Minecraft.getInstance().player)
                .map(cap -> cap.getClientWeaponWarehouse())
                .orElseGet(() -> latestSnapshot.copy());
        this.selectedLoadout = this.snapshot.getSelectedLoadout();
        rebuildCandidates();
    }

    public void replaceSnapshot(WeaponWarehouseSnapshot snapshot) {
        this.snapshot = snapshot;
        this.selectedLoadout = snapshot.getSelectedLoadout();
        rebuildCandidates();
    }

    public static void refreshOpenScreen(WeaponWarehouseSnapshot snapshot) {
        WeaponWarehouseSnapshot clientSnapshot = snapshot.copy();
        clientSnapshot.setSelectedLoadout(WeaponWarehouseItems.clampLoadoutIndex(clientSnapshot.getSelectedLoadout()));
        latestSnapshot = clientSnapshot.copy();
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof WeaponWarehouseScreen warehouseScreen) {
            warehouseScreen.replaceSnapshot(clientSnapshot.copy());
        }
        if (mc.player != null) {
            CommonProxy.getWeaponWarehouseCap(mc.player).ifPresent(cap -> cap.setClientWeaponWarehouse(clientSnapshot.copy()));
        }
    }

    @Override
    protected void init() {
        int panelY = top();
        this.searchBox = new WarehouseSearchBox(this.font, searchX(), panelY + 22, searchWidth(), SEARCH_FIELD_HEIGHT,
                Component.translatable("screen.xtymegamod.weapon_warehouse.search"), SEARCH_PLACEHOLDER);
        this.searchBox.setValue("");
        this.searchBox.setResponder(value -> rebuildCandidates());
        addRenderableWidget(this.searchBox);
        this.loadoutButtons.clear();

        for (int i = 0; i < WeaponWarehouseItems.LOADOUT_COUNT; i++) {
            final int loadout = i;
            WeaponWarehouseButton button = new WeaponWarehouseButton(loadoutButtonX(), panelY + 24 + i * 26, loadoutButtonWidth(), 20,
                    Component.translatable("screen.xtymegamod.weapon_warehouse.loadout", i + 1), pressed -> {
                this.selectedLoadout = loadout;
                this.snapshot.setSelectedLoadout(loadout);
                rebuildCandidates();
            }).setAccent(loadout == this.selectedLoadout);
            this.loadoutButtons.add(addRenderableWidget(button));
        }

        this.applyButton = addRenderableWidget(new WeaponWarehouseButton(searchX(), panelY + panelHeight() - 28, 84, 20,
                Component.translatable("screen.xtymegamod.weapon_warehouse.apply"), button -> {
            this.snapshot.setSelectedLoadout(this.selectedLoadout);
            NetworkHandler.sendToServer(new C2SSaveWeaponWarehousePacket(this.snapshot.copy(), true));
        }).setAccent(true));
        this.saveButton = addRenderableWidget(new WeaponWarehouseButton(searchX() + 92, panelY + panelHeight() - 28, 84, 20,
                Component.translatable("screen.xtymegamod.weapon_warehouse.save"), button -> {
            this.snapshot.setSelectedLoadout(this.selectedLoadout);
            NetworkHandler.sendToServer(new C2SSaveWeaponWarehousePacket(this.snapshot.copy(), false));
        }));
        addRenderableWidget(new WeaponWarehouseButton(searchX() + 184, panelY + panelHeight() - 28, 84, 20,
                Component.translatable("gui.done"), button -> onClose()));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        MegaGuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
        int panelX = left();
        int panelY = top();
        graphics.fill(0, 0, this.width, this.height, 0xD010131A);
        renderWindow(graphics, panelX, panelY, panelWidth(), panelHeight());

        graphics.drawString(this.font, this.title, panelX + 16, panelY + 8, 0xFFE8EEF7);
        graphics.drawString(this.font, Component.translatable("screen.xtymegamod.weapon_warehouse.tip"), searchX(), panelY + 8, 0xFFAAB6C8);
        renderSearchField(graphics, searchX(), panelY + 22, searchWidth(), SEARCH_FIELD_HEIGHT, this.searchBox.isFocused());
        refreshButtons();

        renderSlotGrid(graphics, mouseX, mouseY);
        renderCandidatePanel(graphics, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderSlotGrid(MegaGuiGraphics graphics, int mouseX, int mouseY) {
        int startX = searchX();
        int startY = slotGridY();
        int slotWidth = slotWidth();
        int slotHeight = slotHeight();
        for (WeaponWarehouseSlotType slotType : WeaponWarehouseSlotType.values()) {
            int slot = slotType.getSlotIndex();
            int row = slot / slotColumns();
            int column = slot % slotColumns();
            int x = startX + column * (slotWidth + slotGap());
            int y = startY + row * (slotHeight + 8);
            boolean selected = slotType == this.selectedSlot;
            renderPanelField(graphics, x, y, slotWidth, slotHeight, selected);
            graphics.drawString(this.font, Component.translatable(slotType.getTranslationKey()), x + 6, y + 5, 0xFFE3EDF8);
            ItemStack stack = this.snapshot.getLoadout(this.selectedLoadout).getSlot(slot);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, x + 6, y + 18);
                renderClippedSlotName(graphics, stack, x + 24, y + 22, x + slotWidth - 5, y + slotHeight - 3);
            } else {
                graphics.drawString(this.font, Component.translatable("screen.xtymegamod.weapon_warehouse.empty"), x + 6, y + 22, 0xFF7F8A99);
            }
            if (isWithin(mouseX, mouseY, x, y, slotWidth, slotHeight) && !stack.isEmpty()) {
                graphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }
        }
    }

    private void renderCandidatePanel(MegaGuiGraphics graphics, int mouseX, int mouseY) {
        int x = searchX();
        int y = candidatePanelY();
        int width = contentWidth();
        int height = candidatePanelHeight();
        renderPanelField(graphics, x, y, width, height, false);
        graphics.drawString(this.font, Component.translatable("screen.xtymegamod.weapon_warehouse.candidates"), x + 8, y + 6, 0xFFE8EEF7);

        int listY = y + 24;
        int trackX = x + width - SCROLL_BAR_WIDTH - 5;
        int trackTop = listY;
        int trackHeight = height - 30;
        int visibleCount = Math.max(1, (height - 30) / CANDIDATE_ITEM_HEIGHT);
        int maxScroll = Math.max(0, this.visibleCandidates.size() - visibleCount);
        this.candidateScroll = Mth.clamp(this.candidateScroll, 0, maxScroll);
        for (int i = 0; i < visibleCount; i++) {
            int index = i + this.candidateScroll;
            if (index >= this.visibleCandidates.size()) {
                break;
            }
            ItemStack stack = this.visibleCandidates.get(index);
            int itemY = listY + i * CANDIDATE_ITEM_HEIGHT;
            int itemRight = x + width - CANDIDATE_SCROLL_GUTTER;
            boolean hovered = isWithin(mouseX, mouseY, x + 6, itemY, itemRight - x - 6, 20);
            renderCandidateRow(graphics, x + 6, itemY, itemRight - x - 6, 20, hovered);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, x + 10, itemY + 2);
            } else graphics.blit(ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "textures/ui/fps/locked_button.png"), x + 9, itemY, 0, 0, 20, 20 ,20, 20);
            graphics.drawString(this.font, Component.literal(WeaponWarehouseItems.getDisplayName(stack)), x + 30, itemY + 6, stack.isEmpty() ? 0xFF8B97A7 : 0xFFDDE7F4);
        }
        if (maxScroll > 0) {
            ScrollBarMetrics metrics = scrollBarMetrics(trackTop, trackHeight, visibleCount, maxScroll);
            boolean hovered = isWithin(mouseX, mouseY, trackX - 4, trackTop, SCROLL_BAR_HIT_WIDTH, trackHeight);
            renderSlider(graphics, trackX - 1, trackTop, trackHeight, metrics.thumbTop(), metrics.thumbHeight(), hovered || this.draggingScrollBar);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int startX = searchX();
        int startY = slotGridY();
        int slotWidth = slotWidth();
        int slotHeight = slotHeight();
        for (WeaponWarehouseSlotType slotType : WeaponWarehouseSlotType.values()) {
            int slot = slotType.getSlotIndex();
            int row = slot / slotColumns();
            int column = slot % slotColumns();
            int x = startX + column * (slotWidth + slotGap());
            int y = startY + row * (slotHeight + 8);
            if (isWithin(mouseX, mouseY, x, y, slotWidth, slotHeight)) {
                this.selectedSlot = slotType;
                rebuildCandidates();
                return true;
            }
        }

        int listX = searchX() + 6;
        int listY = candidatePanelY() + 24;
        int width = contentWidth() - CANDIDATE_SCROLL_GUTTER - 6;
        int trackX = searchX() + contentWidth() - SCROLL_BAR_WIDTH - 5;
        int trackTop = candidatePanelY() + 24;
        int trackHeight = candidatePanelHeight() - 30;
        int visibleCount = Math.max(1, (candidatePanelHeight() - 30) / CANDIDATE_ITEM_HEIGHT);
        int maxScroll = Math.max(0, this.visibleCandidates.size() - visibleCount);
        if (maxScroll > 0 && isWithin(mouseX, mouseY, trackX - 4, trackTop, SCROLL_BAR_HIT_WIDTH, trackHeight)) {
            this.draggingScrollBar = true;
            ScrollBarMetrics metrics = scrollBarMetrics(trackTop, trackHeight, visibleCount, maxScroll);
            this.scrollBarGrabOffset = (float) Mth.clamp(mouseY - metrics.thumbTop(), 0.0D, metrics.thumbHeight());
            updateScrollFromMouse(mouseY, trackTop, trackHeight, visibleCount, maxScroll);
            return true;
        }
        for (int i = 0; i < visibleCount; i++) {
            int index = i + this.candidateScroll;
            if (index >= this.visibleCandidates.size()) {
                break;
            }
            int y = listY + i * CANDIDATE_ITEM_HEIGHT;
            if (isWithin(mouseX, mouseY, listX, y, width, 20)) {
                this.snapshot.getLoadout(this.selectedLoadout).setSlot(this.selectedSlot.getSlotIndex(), this.visibleCandidates.get(index));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingScrollBar) {
            int visibleCount = Math.max(1, (candidatePanelHeight() - 30) / CANDIDATE_ITEM_HEIGHT);
            int maxScroll = Math.max(0, this.visibleCandidates.size() - visibleCount);
            updateScrollFromMouse(mouseY, candidatePanelY() + 24, candidatePanelHeight() - 30, visibleCount, maxScroll);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingScrollBar = false;
        this.scrollBarGrabOffset = 0.0F;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int x = searchX();
        int y = candidatePanelY();
        int width = contentWidth();
        int height = candidatePanelHeight();
        if (isWithin(mouseX, mouseY, x, y, width, height)) {
            this.candidateScroll -= (int) Math.signum(delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void rebuildCandidates() {
        String search = this.searchBox == null ? "" : this.searchBox.getValue().trim().toLowerCase();
        List<ItemStack> base = WeaponWarehouseItems.createClientCandidates(this.selectedSlot);
        if (search.isEmpty()) {
            this.visibleCandidates = base;
        } else {
            List<ItemStack> filtered = new ArrayList<>();
            for (ItemStack stack : base) {
                if (WeaponWarehouseItems.createSearchText(stack).contains(search)) {
                    filtered.add(stack);
                }
            }
            this.visibleCandidates = filtered;
        }
        this.candidateScroll = 0;
    }

    private int left() {
        return (this.width - panelWidth()) / 2;
    }

    private int top() {
        return (this.height - panelHeight()) / 2;
    }

    private int panelWidth() {
        return Mth.clamp(this.width - 24, 320, 430);
    }

    private int panelHeight() {
        return Mth.clamp(this.height - 24, 250, 330);
    }

    private int contentWidth() {
        return panelWidth() - searchXOffset() - 16;
    }

    private int slotWidth() {
        return (contentWidth() - slotGap() * (slotColumns() - 1)) / slotColumns();
    }

    private int slotHeight() {
        return 40;
    }

    private int slotGap() {
        return 8;
    }

    private int slotColumns() {
        return 3;
    }

    private int slotGridY() {
        return top() + 56;
    }

    private int searchX() {
        return left() + searchXOffset();
    }

    private int searchWidth() {
        return contentWidth();
    }

    private int searchXOffset() {
        return 122;
    }

    private int loadoutButtonX() {
        return left() + 16;
    }

    private int loadoutButtonWidth() {
        return 64;
    }

    private int candidatePanelY() {
        return slotGridY() + slotRows() * slotHeight() + (slotRows() - 1) * 8 + 10;
    }

    private int candidatePanelHeight() {
        return Math.max(54, top() + panelHeight() - 36 - candidatePanelY());
    }

    private void updateScrollFromMouse(double mouseY, int trackTop, int trackHeight, int visibleCount, int maxScroll) {
        if (maxScroll <= 0) {
            this.candidateScroll = 0;
            return;
        }
        int thumbHeight = scrollBarMetrics(trackTop, trackHeight, visibleCount, maxScroll).thumbHeight();
        int thumbRange = Math.max(1, trackHeight - thumbHeight);
        float relative = (float) (mouseY - trackTop - this.scrollBarGrabOffset) / (float) thumbRange;
        this.candidateScroll = Mth.clamp(Mth.floor(relative * maxScroll + 0.5F), 0, maxScroll);
    }

    private ScrollBarMetrics scrollBarMetrics(int trackTop, int trackHeight, int visibleCount, int maxScroll) {
        int thumbHeight = Math.max(14, (int) ((visibleCount / (float) this.visibleCandidates.size()) * trackHeight));
        int thumbRange = Math.max(0, trackHeight - thumbHeight);
        int thumbOffset = thumbRange <= 0 ? 0 : Mth.floor((this.candidateScroll / (float) maxScroll) * thumbRange);
        return new ScrollBarMetrics(trackTop + thumbOffset, thumbHeight);
    }

    private void renderClippedSlotName(MegaGuiGraphics graphics, ItemStack stack, int x, int y, int right, int bottom) {
        graphics.enableScissor(x, y, right, bottom);
        graphics.drawString(this.font, Component.literal(WeaponWarehouseItems.getDisplayName(stack)), x, y, 0xFFCDD9E6);
        graphics.disableScissor();
    }

    private int slotRows() {
        return (WeaponWarehouseItems.SLOT_COUNT + slotColumns() - 1) / slotColumns();
    }

    private void refreshButtons() {
        for (int i = 0; i < this.loadoutButtons.size(); i++) {
            this.loadoutButtons.get(i).setAccent(i == this.selectedLoadout);
        }
        if (this.applyButton != null) {
            this.applyButton.setAccent(true);
        }
    }

    private void renderWindow(MegaGuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xCC0B0D13);
        graphics.fill(x, y, x + width, y + height, 0xE1141822);
        graphics.blitNineSlicedSized(GUI_PANEL, x, y, width, height, 5, PANEL_TEXTURE_SIZE, PANEL_TEXTURE_SIZE, 0, 0, 256, 256);
    }

    private void renderSearchField(MegaGuiGraphics graphics, int x, int y, int width, int height, boolean focused) {
        graphics.blit(GUI_PANEL, x, y, width, height, 0.0F, 96F, 200.0F, 20.0F, 256, 256);
    }

    private void renderPanelField(MegaGuiGraphics graphics, int x, int y, int width, int height, boolean selected) {
        graphics.blitNineSlicedSized(selected ? GUI_PANEL_HIGHLIGHT : GUI_PANEL, x, y, width, height, 5, PANEL_TEXTURE_SIZE, PANEL_TEXTURE_SIZE, 0, 0, 256, 256);
    }

    private void renderCandidateRow(MegaGuiGraphics graphics, int x, int y, int width, int height, boolean hovered) {
        graphics.fill(x, y, x + width, y + height, hovered ? 0x663B4960 : 0x4411171F);
    }

    private void renderSlider(MegaGuiGraphics graphics, int x, int y, int height, int thumbTop, int thumbHeight, boolean hovered) {
        if (height <= 0 || thumbHeight <= 0) {
            return;
        }
        graphics.fill(x, y, x + SCROLL_BAR_WIDTH, y + height, 0x9911171F);
        ResourceLocation handle = hovered ? SLIDER_HANDLE_HIGHLIGHTED : SLIDER_HANDLE;
        graphics.blitNineSlicedSized(
                handle,
                x,
                thumbTop,
                SCROLL_BAR_WIDTH,
                thumbHeight,
                SCROLL_BAR_SLICE,
                SCROLL_BAR_WIDTH,
                SCROLL_HANDLE_TEXTURE_HEIGHT,
                0,
                0,
                SCROLL_BAR_WIDTH,
                SCROLL_HANDLE_TEXTURE_HEIGHT
        );
    }

    private void blitRegion(MegaGuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        if (width <= 0 || height <= 0 || regionWidth <= 0 || regionHeight <= 0) {
            return;
        }
        graphics.blit(texture, x, y, width, height, (float) u, (float) v, (float) regionWidth, (float) regionHeight, (float) textureWidth, (float) textureHeight);
    }

    private static boolean isWithin(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private record ScrollBarMetrics(int thumbTop, int thumbHeight) {
    }

    private static class WarehouseSearchBox extends EditBox {
        private final Font displayFont;
        private final Component placeholder;

        private WarehouseSearchBox(Font font, int x, int y, int width, int height, Component message, Component placeholder) {
            super(font, x, y, width, height, message);
            this.displayFont = font;
            this.placeholder = placeholder;
            this.setBordered(false);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int originalY = this.getY();
            int textY = originalY + (this.getHeight() - this.displayFont.lineHeight) / 2;
            this.setY(textY);
            if (this.getValue().isEmpty()) {
                graphics.drawString(this.displayFont, this.placeholder, this.getX() + 9, textY, 0xFF738093);
            }
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(9, 0, 0);
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);
            poseStack.popPose();
            this.setY(originalY);
        }
    }
}
