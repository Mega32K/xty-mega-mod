package com.mega.xty.util.data_expand;

import com.google.common.base.MoreObjects;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.xty.common.data.map2.ClientGameData;
import com.mega.xty.common.init.ItemInit;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface OpticalNanoInvisibleItemInHandRenderer {
    void opticalNanoInvisible(float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int light);
}
