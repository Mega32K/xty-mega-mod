package com.mega.xty.mixin.client;

import com.mega.xty.client.screen.map2.Game2ClientOptionsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void xtyMegaMod$addGame2ClientOptionsButton(CallbackInfo ci) {
        int x = this.width / 2 - 102 - 24;
        int y = this.height / 4 + 64 - 16;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.xtymegamod.map2game2.client_options.open_short"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new Game2ClientOptionsScreen((Screen) (Object) this));
            }
        }).bounds(x, y, 20, 20).tooltip(Tooltip.create(Component.translatable("screen.xtymegamod.map2game2.client_options.open_tooltip"))).build());
    }
}
