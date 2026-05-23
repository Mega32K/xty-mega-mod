package com.mega.map.client.screen.map2;

import com.mega.endinglib.client.ClientWrapped;
import com.mega.map.common.capability.FpsCapability;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.c2s.map2game2.C2SSetGame2ClientOptionPacket;
import com.mega.map.proxy.CommonProxy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Game2ClientOptionsCategoryScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_SPACING = 24;

    private final Screen lastScreen;
    private final Category category;

    public Game2ClientOptionsCategoryScreen(Screen lastScreen, Category category) {
        super(Component.translatable(category.titleKey));
        this.lastScreen = lastScreen;
        this.category = category;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - 155;
        int rightX = this.width / 2 + 5;
        int y = this.height / 6 - 12;
        for (int i = 0; i < this.category.options.length; i++) {
            ClientOption option = this.category.options[i];
            int x = i % 2 == 0 ? leftX : rightX;
            int row = i / 2;
            addBooleanOption(x, y + row * ROW_SPACING, option);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 - 100, this.height / 6 + 168, 200, BUTTON_HEIGHT)
                .build());
    }

    private void addBooleanOption(int x, int y, ClientOption option) {
        boolean current = getCurrentValue(option.getter);
        this.addRenderableWidget(CycleButton.onOffBuilder(current)
                .create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(option.translationKey), (button, value) -> {
                    NetworkHandler.sendToServer(new C2SSetGame2ClientOptionPacket(option.networkKey, value));
                }));
    }

    private boolean getCurrentValue(Function<FpsCapability, Boolean> getter) {
        if (ClientWrapped.clientPlayer() == null) {
            return false;
        }
        return CommonProxy.getFPSCap(ClientWrapped.clientPlayer()).map(getter::apply).orElse(false);
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

    public enum Category {
        HUD("screen.megamod.map2game2.client_options.section.hud",
                new ClientOption("screen.megamod.map2game2.client_options.hud.bomb_countdown_prompt", "hud.bombCountdownPrompt", cap -> cap.getGame2ClientOptions().hud().showBombCountdownPrompt()),
                new ClientOption("screen.megamod.map2game2.client_options.hud.bomb_progress_bar", "hud.bombProgressBar", cap -> cap.getGame2ClientOptions().hud().showBombProgressBar()),
                new ClientOption("screen.megamod.map2game2.client_options.hud.round_start_prompt", "hud.roundStartPrompt", cap -> cap.getGame2ClientOptions().hud().showRoundStartPrompt()),
                new ClientOption("screen.megamod.map2game2.client_options.hud.round_result_overlay", "hud.roundResultOverlay", cap -> cap.getGame2ClientOptions().hud().showRoundResultOverlay()),
                new ClientOption("screen.megamod.map2game2.client_options.hud.round_mvp_overlay", "hud.roundMvpOverlay", cap -> cap.getGame2ClientOptions().hud().showRoundMvpOverlay())),
        VISUAL("screen.megamod.map2game2.client_options.section.visual",
                new ClientOption("screen.megamod.map2game2.client_options.visual.aspect43", "visual.aspect43", FpsCapability::isAspect43),
                new ClientOption("screen.megamod.map2game2.client_options.visual.round_start_post_effect", "visual.roundStartPostEffect", cap -> cap.getGame2ClientOptions().visual().useRoundStartPostEffect()),
                new ClientOption("screen.megamod.map2game2.client_options.visual.dead_post_effect", "visual.deadPostEffect", cap -> cap.getGame2ClientOptions().visual().useDeadPostEffect()),
                new ClientOption("screen.megamod.map2game2.client_options.visual.death_camera", "visual.deathCamera", cap -> cap.getGame2ClientOptions().visual().useDeathCamera()),
                new ClientOption("screen.megamod.map2game2.client_options.visual.c4_spectate_camera", "visual.c4SpectateCamera", cap -> cap.getGame2ClientOptions().visual().useC4SpectateCamera())),
        AUDIO("screen.megamod.map2game2.client_options.section.audio",
                new ClientOption("screen.megamod.map2game2.client_options.audio.bomb_beep", "audio.bombBeep", cap -> cap.getGame2ClientOptions().audio().playBombBeep()),
                new ClientOption("screen.megamod.map2game2.client_options.audio.round_result_sound", "audio.roundResultSound", cap -> cap.getGame2ClientOptions().audio().playRoundResultSound()));

        private final String titleKey;
        private final ClientOption[] options;

        Category(String titleKey, ClientOption... options) {
            this.titleKey = titleKey;
            this.options = options;
        }
    }

    private record ClientOption(String translationKey, String networkKey, Function<FpsCapability, Boolean> getter) {
    }
}
