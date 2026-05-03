package com.mega.xty.mixin.map2.game2;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.SafeClass;
import com.mega.endinglib.util.annotation.ModDependsMixin;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.util.data_expand.OpticalNanoInvisibleItemInHandRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
