package com.mega.map.mixin.map2.game2;

import com.mega.map.common.data.map2.ClientGame2Data;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private LightTexture lightTexture;

    @Shadow @Final public ItemInHandRenderer itemInHandRenderer;

    @Shadow @Final private RenderBuffers renderBuffers;

    @Shadow private boolean panoramicMode;

    @Shadow public abstract void resetProjectionMatrix(Matrix4f p_253668_);

    @Shadow public abstract Matrix4f getProjectionMatrix(double p_254507_);

    @Shadow protected abstract double getFov(Camera p_109142_, float p_109143_, boolean p_109144_);

    @Shadow protected abstract void bobHurt(PoseStack p_109118_, float p_109119_);

    @Shadow protected abstract void bobView(PoseStack p_109139_, float p_109140_);

    @Shadow @Final private Camera mainCamera;

    /*
        @Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z", shift = At.Shift.AFTER))
        private void preventShaderDisableRenderingArm(PoseStack p_109121_, Camera p_109122_, float p_109123_, CallbackInfo ci) {
            Player localP = ClientWrapped.clientPlayer();
            if (localP.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.OPTICAL_NANOSUIT.get())) {
                boolean flag = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
                if (this.minecraft.options.getCameraType().isFirstPerson() && !flag && !this.minecraft.options.hideGui && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                    this.lightTexture.turnOnLightLayer();
                    ((OpticalNanoInvisibleItemInHandRenderer) itemInHandRenderer).opticalNanoInvisible(p_109123_, p_109121_, minecraft.renderBuffers().bufferSource(), this.minecraft.player, this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, p_109123_));
                    this.lightTexture.turnOffLightLayer();
                }
            }
        }
         */
    @Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
    private static void getNightVisionScale(LivingEntity p_109109_, float p_109110_, CallbackInfoReturnable<Float> cir) {
        if (ClientGame2Data.playing()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.cameraEntity instanceof Player player) {
                if (mc.level != null && mc.level.dayTime() > 12300)
                    cir.setReturnValue(1F);
            }
        }
    }
}
