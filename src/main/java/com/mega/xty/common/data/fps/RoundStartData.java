package com.mega.xty.common.data.fps;

import com.mega.endinglib.api.client.Easing;

public class RoundStartData {
    public static final int ROUND_START_PROMPT_DURATION = 11 * 20;
    private static final int ROUND_START_COUNTDOWN_SECONDS = 10;
    public static int roundStartRenderTimer;

    public static void tick() {
        if (roundStartRenderTimer > 0) {
            roundStartRenderTimer--;
        }
    }

    public static void requestRoundStartRender() {
        roundStartRenderTimer = ROUND_START_PROMPT_DURATION;
    }

    public static void stop() {
        roundStartRenderTimer = 0;
    }

    public static boolean shouldRenderRoundStart() {
        return roundStartRenderTimer > 0;
    }

    public static int getDisplayCountdownSeconds(float partialTicks) {
        float elapsedTicks = ROUND_START_PROMPT_DURATION - roundStartRenderTimer + partialTicks;
        int elapsedSeconds = Math.min(ROUND_START_COUNTDOWN_SECONDS, Math.max(0, (int) (elapsedTicks / 20.0F)));
        return ROUND_START_COUNTDOWN_SECONDS - elapsedSeconds;
    }

    public static float getRoundStartNotificationAlpha(float partialTicks) {
        float fadeIn = Math.min(1.0F, Math.min(roundStartRenderTimer - partialTicks, 10.0F) / 10.0F);
        float fadeOut = Math.min(1.0F, (ROUND_START_PROMPT_DURATION - roundStartRenderTimer + partialTicks) / 10.0F);
        return Easing.OUT_CUBIC.calculate(fadeIn) * Easing.OUT_CUBIC.calculate(fadeOut);
    }
}
