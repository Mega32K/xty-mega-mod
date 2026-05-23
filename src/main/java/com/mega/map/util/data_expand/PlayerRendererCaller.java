package com.mega.map.util.data_expand;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

public interface PlayerRendererCaller {
    void renderWithoutEvents(AbstractClientPlayer clientPlayer, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light);
}
