package com.mega.map.util.data_expand;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

public interface OpticalNanoInvisibleItemInHandRenderer {
    void opticalNanoInvisible(float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int light);
}
