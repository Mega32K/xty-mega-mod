package com.mega.xty.common.capability;

import com.mega.endinglib.api.capability.CapabilityEntityData;
import com.mega.endinglib.api.capability.CapabilitySyncType;
import com.mega.endinglib.api.capability.EntitySyncCapabilityBase;
import com.mega.endinglib.api.capability.syncher.CapabilityDataSerializers;
import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.options.map2game2.Game2ClientOptions;
import com.mega.xty.common.network.s2c.map2.S2CAddDeathDataPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class FpsCapability extends EntitySyncCapabilityBase {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(XtyMegaMod.MODID, "fps");
    public final CapabilityEntityData<Boolean> ASPECT_43 = this.dataManager.define(0, "aspect43", false, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> HUD_BOMB_COUNTDOWN_PROMPT = this.dataManager.define(1, "hudBombCountdownPrompt", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> HUD_BOMB_PROGRESS_BAR = this.dataManager.define(2, "hudBombProgressBar", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> HUD_ROUND_START_PROMPT = this.dataManager.define(3, "hudRoundStartPrompt", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> HUD_ROUND_RESULT_OVERLAY = this.dataManager.define(4, "hudRoundResultOverlay", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> HUD_ROUND_MVP_OVERLAY = this.dataManager.define(5, "hudRoundMvpOverlay", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> VISUAL_ROUND_START_POST_EFFECT = this.dataManager.define(6, "visualRoundStartPostEffect", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> VISUAL_DEAD_POST_EFFECT = this.dataManager.define(7, "visualDeadPostEffect", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> VISUAL_DEATH_CAMERA = this.dataManager.define(8, "visualDeathCamera", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> VISUAL_C4_SPECTATE_CAMERA = this.dataManager.define(9, "visualC4SpectateCamera", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> AUDIO_BOMB_BEEP = this.dataManager.define(10, "audioBombBeep", true, CapabilityDataSerializers.BOOLEAN);
    public final CapabilityEntityData<Boolean> AUDIO_ROUND_RESULT_SOUND = this.dataManager.define(11, "audioRoundResultSound", true, CapabilityDataSerializers.BOOLEAN);
    private final Game2ClientOptions game2ClientOptions = new Game2ClientOptions(this);
    @Nullable
    private UUID assisterID;
    /**
     * 助攻最高
     */
    @Nullable
    private Player assister;
    private float assisterDamage;
    @Nullable
    private UUID assisterID2;
    /**
     * 助攻第二高
     */
    @Nullable
    private Player assister2;
    private float assisterDamage2;
    public final Map<UUID, DeathMessage> deathMessageMap = new Object2ObjectOpenHashMap<>();
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }
    @Override
    protected @NotNull Predicate<Entity> canAttach() {
        return entity -> entity instanceof Player;
    }
    @Override
    public void onSyncedDataUpdated(CapabilityEntityData<?> data) {
    }

    @Override
    public void syncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {
    }
    @Override
    public void readSyncData(CompoundTag compoundTag, Dist dist, CapabilitySyncType capabilitySyncType, Entity entity) {

    }

    @Override
    public boolean canSyncWhenTick(Entity entity, Level level) {
        return false;
    }

    @Override
    public void customSerializeNBT(CompoundTag compoundTag) {
        if (this.assisterDamage > 0.0F)
            compoundTag.putFloat("assisterDamage", this.assisterDamage);
        if (this.assisterID != null)
            compoundTag.putUUID("assister", this.assisterID);
        if (this.assisterDamage2 > 0.0F)
            compoundTag.putFloat("assisterDamage2", this.assisterDamage2);
        if (this.assisterID2 != null)
            compoundTag.putUUID("assister2", this.assisterID2);
    }

    @Override
    public void customDeserializeNBT(CompoundTag compoundTag) {
        if (CompoundTagUtils.containsFloat(compoundTag, "assisterDamage"))
            this.assisterDamage = compoundTag.getFloat("assisterDamage");
        if (CompoundTagUtils.containsFloat(compoundTag, "assisterDamage2"))
            this.assisterDamage2 = compoundTag.getFloat("assisterDamage2");
        if (compoundTag.hasUUID("assister")) {
            this.assisterID = compoundTag.getUUID("assister");
            if (this.getEntity() instanceof Player player)
                this.assister =  player.level().getPlayerByUUID(this.assisterID);
        }
        if (compoundTag.hasUUID("assister2")) {
            this.assisterID2 = compoundTag.getUUID("assister2");
            if (this.getEntity() instanceof Player player)
                this.assister2 =  player.level().getPlayerByUUID(this.assisterID2);
        }
    }

    @Override
    protected void tick(Entity entity) {
        if (entity instanceof Player player) {
            if (!this.deathMessageMap.isEmpty()) {
                synchronized (deathMessageMap) {
                    if (!this.deathMessageMap.isEmpty()) {
                        for (var message : deathMessageMap.values()) {
                            if (message.killedMessageKiller != null && !message.killedMessageTypes.isEmpty() && message.killedMessageKilled != null) {
                                NetworkHandler.sendToAll(new S2CAddDeathDataPacket(message.killedMessageKiller, message.killedWeapon, message.makeDeathTypeComponent().append(message.killedMessageKilled)));
                            }
                        }
                        deathMessageMap.clear();
                    }
                }
            }
            if (player instanceof ServerPlayer sp && sp.tickCount % 20 == 0) {
                FpsSavedData fpsSavedData = FpsSavedData.getInstance(sp.server);
                fpsSavedData.setPlayerTab(sp);
            }
        }
    }

    public DeathMessage getOrDefaultDeathMessage(Entity entity) {
        DeathMessage deathMessage = deathMessageMap.get(entity.getUUID());
        if (deathMessage == null) {
            deathMessage = new DeathMessage();
            deathMessageMap.put(entity.getUUID(), deathMessage);
        }
        return deathMessage;
    }

    @Nullable
    public Player checkAndGetAssister(Level level) {
        if (this.assister == null && this.assisterID != null)
            this.assister = level.getPlayerByUUID(this.assisterID);
        return assister;
    }
    public void setAssister(@Nullable Player assister) {
        if (assister == null) {
            this.assister = null;
            this.assisterID = null;
        } else {
            this.assister = assister;
            this.assisterID = assister.getUUID();
        }
    }

    @Nullable
    public Player checkAndGetAssister2(Level level) {
        if (this.assister2 == null && this.assisterID2 != null)
            this.assister2 = level.getPlayerByUUID(this.assisterID2);
        return assister2;
    }
    public void setAssister2(@Nullable Player assister) {
        if (assister == null) {
            this.assister2 = null;
            this.assisterID2 = null;
        } else {
            this.assister2 = assister;
            this.assisterID2 = assister.getUUID();
        }
    }
    public float getAssisterDamage() {
        return assisterDamage;
    }

    public void setAssisterDamage(float assisterDamage) {
        this.assisterDamage = assisterDamage;
    }
    public float getAssisterDamage2() {
        return assisterDamage2;
    }

    public void setAssisterDamage2(float assisterDamage) {
        this.assisterDamage2 = assisterDamage;
    }

    public boolean isAspect43() {
        return this.dataManager.getValue(ASPECT_43);
    }

    public void setAspect43(boolean enabled) {
        this.dataManager.setValue(ASPECT_43, enabled);
    }

    public boolean isHudBombCountdownPromptEnabled() {
        return this.dataManager.getValue(HUD_BOMB_COUNTDOWN_PROMPT);
    }

    public void setHudBombCountdownPromptEnabled(boolean enabled) {
        this.dataManager.setValue(HUD_BOMB_COUNTDOWN_PROMPT, enabled);
    }

    public boolean isHudBombProgressBarEnabled() {
        return this.dataManager.getValue(HUD_BOMB_PROGRESS_BAR);
    }

    public void setHudBombProgressBarEnabled(boolean enabled) {
        this.dataManager.setValue(HUD_BOMB_PROGRESS_BAR, enabled);
    }

    public boolean isHudRoundStartPromptEnabled() {
        return this.dataManager.getValue(HUD_ROUND_START_PROMPT);
    }

    public void setHudRoundStartPromptEnabled(boolean enabled) {
        this.dataManager.setValue(HUD_ROUND_START_PROMPT, enabled);
    }

    public boolean isHudRoundResultOverlayEnabled() {
        return this.dataManager.getValue(HUD_ROUND_RESULT_OVERLAY);
    }

    public void setHudRoundResultOverlayEnabled(boolean enabled) {
        this.dataManager.setValue(HUD_ROUND_RESULT_OVERLAY, enabled);
    }

    public boolean isHudRoundMvpOverlayEnabled() {
        return this.dataManager.getValue(HUD_ROUND_MVP_OVERLAY);
    }

    public void setHudRoundMvpOverlayEnabled(boolean enabled) {
        this.dataManager.setValue(HUD_ROUND_MVP_OVERLAY, enabled);
    }

    public boolean isVisualRoundStartPostEffectEnabled() {
        return this.dataManager.getValue(VISUAL_ROUND_START_POST_EFFECT);
    }

    public void setVisualRoundStartPostEffectEnabled(boolean enabled) {
        this.dataManager.setValue(VISUAL_ROUND_START_POST_EFFECT, enabled);
    }

    public boolean isVisualDeadPostEffectEnabled() {
        return this.dataManager.getValue(VISUAL_DEAD_POST_EFFECT);
    }

    public void setVisualDeadPostEffectEnabled(boolean enabled) {
        this.dataManager.setValue(VISUAL_DEAD_POST_EFFECT, enabled);
    }

    public boolean isVisualDeathCameraEnabled() {
        return this.dataManager.getValue(VISUAL_DEATH_CAMERA);
    }

    public void setVisualDeathCameraEnabled(boolean enabled) {
        this.dataManager.setValue(VISUAL_DEATH_CAMERA, enabled);
    }

    public boolean isVisualC4SpectateCameraEnabled() {
        return this.dataManager.getValue(VISUAL_C4_SPECTATE_CAMERA);
    }

    public void setVisualC4SpectateCameraEnabled(boolean enabled) {
        this.dataManager.setValue(VISUAL_C4_SPECTATE_CAMERA, enabled);
    }

    public boolean isAudioBombBeepEnabled() {
        return this.dataManager.getValue(AUDIO_BOMB_BEEP);
    }

    public void setAudioBombBeepEnabled(boolean enabled) {
        this.dataManager.setValue(AUDIO_BOMB_BEEP, enabled);
    }

    public boolean isAudioRoundResultSoundEnabled() {
        return this.dataManager.getValue(AUDIO_ROUND_RESULT_SOUND);
    }

    public void setAudioRoundResultSoundEnabled(boolean enabled) {
        this.dataManager.setValue(AUDIO_ROUND_RESULT_SOUND, enabled);
    }

    public Game2ClientOptions getGame2ClientOptions() {
        return this.game2ClientOptions;
    }
}
