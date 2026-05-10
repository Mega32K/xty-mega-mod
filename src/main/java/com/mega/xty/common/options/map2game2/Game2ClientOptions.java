package com.mega.xty.common.options.map2game2;

import com.mega.xty.common.capability.FpsCapability;

public class Game2ClientOptions {
    private final FpsCapability capability;
    private final Hud hud = new Hud();
    private final Visual visual = new Visual();
    private final Audio audio = new Audio();

    public Game2ClientOptions(FpsCapability capability) {
        this.capability = capability;
    }

    public Hud hud() {
        return this.hud;
    }

    public Visual visual() {
        return this.visual;
    }

    public Audio audio() {
        return this.audio;
    }

    public final class Hud {
        public boolean showBombCountdownPrompt() {
            return capability.isHudBombCountdownPromptEnabled();
        }

        public void setShowBombCountdownPrompt(boolean enabled) {
            capability.setHudBombCountdownPromptEnabled(enabled);
        }

        public boolean showBombProgressBar() {
            return capability.isHudBombProgressBarEnabled();
        }

        public void setShowBombProgressBar(boolean enabled) {
            capability.setHudBombProgressBarEnabled(enabled);
        }

        public boolean showRoundStartPrompt() {
            return capability.isHudRoundStartPromptEnabled();
        }

        public void setShowRoundStartPrompt(boolean enabled) {
            capability.setHudRoundStartPromptEnabled(enabled);
        }

        public boolean showRoundResultOverlay() {
            return capability.isHudRoundResultOverlayEnabled();
        }

        public void setShowRoundResultOverlay(boolean enabled) {
            capability.setHudRoundResultOverlayEnabled(enabled);
        }

        public boolean showRoundMvpOverlay() {
            return capability.isHudRoundMvpOverlayEnabled();
        }

        public void setShowRoundMvpOverlay(boolean enabled) {
            capability.setHudRoundMvpOverlayEnabled(enabled);
        }
    }

    public final class Visual {
        public boolean useAspect43() {
            return capability.isAspect43();
        }

        public void setUseAspect43(boolean enabled) {
            capability.setAspect43(enabled);
        }

        public boolean useRoundStartPostEffect() {
            return capability.isVisualRoundStartPostEffectEnabled();
        }

        public void setUseRoundStartPostEffect(boolean enabled) {
            capability.setVisualRoundStartPostEffectEnabled(enabled);
        }

        public boolean useDeadPostEffect() {
            return capability.isVisualDeadPostEffectEnabled();
        }

        public void setUseDeadPostEffect(boolean enabled) {
            capability.setVisualDeadPostEffectEnabled(enabled);
        }

        public boolean useDeathCamera() {
            return capability.isVisualDeathCameraEnabled();
        }

        public void setUseDeathCamera(boolean enabled) {
            capability.setVisualDeathCameraEnabled(enabled);
        }

        public boolean useC4SpectateCamera() {
            return capability.isVisualC4SpectateCameraEnabled();
        }

        public void setUseC4SpectateCamera(boolean enabled) {
            capability.setVisualC4SpectateCameraEnabled(enabled);
        }
    }

    public final class Audio {
        public boolean playBombBeep() {
            return capability.isAudioBombBeepEnabled();
        }

        public void setPlayBombBeep(boolean enabled) {
            capability.setAudioBombBeepEnabled(enabled);
        }

        public boolean playRoundResultSound() {
            return capability.isAudioRoundResultSoundEnabled();
        }

        public void setPlayRoundResultSound(boolean enabled) {
            capability.setAudioRoundResultSoundEnabled(enabled);
        }
    }
}
