package com.mega.map.client.screen.map2;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class Game2ClientOptionsScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    private final Screen lastScreen;

    public Game2ClientOptionsScreen(Screen lastScreen) {
        super(Component.translatable("screen.megamod.map2game2.client_options.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 6 - 12;

        GridLayout gridlayout = new GridLayout();
        gridlayout.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper gridlayout$rowhelper = gridlayout.createRowHelper(2);
        gridlayout$rowhelper.addChild(Button.builder(Component.translatable("screen.megamod.map2game2.client_options.section.hud"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new Game2ClientOptionsCategoryScreen(this, Game2ClientOptionsCategoryScreen.Category.HUD));
            }
        }).bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_SPACING;

        gridlayout$rowhelper.addChild(Button.builder(Component.translatable("screen.megamod.map2game2.client_options.section.visual"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new Game2ClientOptionsCategoryScreen(this, Game2ClientOptionsCategoryScreen.Category.VISUAL));
            }
        }).bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_SPACING;

        gridlayout$rowhelper.addChild(Button.builder(Component.translatable("screen.megamod.map2game2.client_options.section.audio"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new Game2ClientOptionsCategoryScreen(this, Game2ClientOptionsCategoryScreen.Category.AUDIO));
            }
        }).bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        gridlayout$rowhelper.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .width(200)
                .build(), 2, gridlayout$rowhelper.newCellSettings().paddingTop(6));

        gridlayout.arrangeElements();
        FrameLayout.alignInRectangle(gridlayout, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
        gridlayout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }
}
