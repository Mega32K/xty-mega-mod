package com.mega.map.mixin.map1.game_2;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mega.map.common.data.map1.ClientGame2Data;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.c2s.map1.game2.C2SPlayerJumpPacket;
import com.mega.map.proxy.CommonProxy;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    @Shadow
    public Input input;
    @Unique
    private boolean xty$wasJumping;

    LocalPlayerMixin(ClientLevel p_250460_, GameProfile p_249912_) {
        super(p_250460_, p_249912_);
    }

    @Inject(method = "aiStep", at = {@At("TAIL")})
    private void handleMultiJump(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        boolean isOnGround = player.onGround();
        CommonProxy.getXtyCap(player).ifPresent(capability -> {
            if (!ClientGame2Data.isStopped) {
                boolean canJump = false;
                if (capability.xlCollision) {
                    canJump = true;
                    capability.verticalGame2MultiJumpTime = 5;
                }
                if ((player.verticalCollision && !player.verticalCollisionBelow)) {
                    canJump = true;
                    capability.verticalGame2MultiJumpTime = 7;
                } else if (capability.verticalGame2MultiJumpTime > 0) {
                    canJump = true;
                }

                if (!player.isCreative() && !player.isSpectator() && canJump) {
                    boolean isJumping = this.input.jumping;
                    if (isJumping && !this.xty$wasJumping &&
                            !isOnGround) {
                        player.jumpFromGround();
                        player.fallDistance = 0.0F;
                        NetworkHandler.sendToServer(new C2SPlayerJumpPacket());
                    }
                    this.xty$wasJumping = isJumping;
                }
            }
        });
    }
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=0.2F"))
    private float noUsingSlowdown(float original) {
        if (!ClientGame2Data.isStopped) return 1F;
        return original;
    }
}
