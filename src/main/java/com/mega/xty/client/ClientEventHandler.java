package com.mega.xty.client;

import com.mega.endinglib.api.client.LambdaClientTaskInstance;
import com.mega.xty.common.item.FillFunctionCreatorItem;
import com.mega.xty.proxy.ClientProxy;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

//@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandler {
    public static Pair<Boolean, VoxelShape> selected = Pair.of(false, null);
    public static VoxelShape selecting;
    public static AABB selectingAABB;
    public static BlockPos[] selectingPos = new BlockPos[2];
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START)  {
            selectingPos[0] = selectingPos[1] = null;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FillFunctionCreatorItem) {
                ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
                CompoundTag nbt = itemStack.getTag();
                if (nbt != null) {
                    CompoundTag interaction = nbt.getCompound(FillFunctionCreatorItem.INTERACTION);
                    if (!interaction.isEmpty()) {
                        BlockPos start = FillFunctionCreatorItem.fromArray(interaction.getIntArray("0"));
                        BlockPos end = FillFunctionCreatorItem.fromArray(interaction.getIntArray("1"));
                        selectingPos[0] = start;
                        if (end != null)
                            selectingPos[1] = end;
                        else if (mc.hitResult instanceof BlockHitResult bhr) {
                            selectingPos[1] = bhr.getBlockPos();
                        } else selectingPos[1] = null;
                    }
                }
                if (selectingPos[0] != null && selectingPos[1] != null) {
                    selectingAABB = new AABB(selectingPos[0], selectingPos[1]);
                    selecting = Shapes.create(selectingAABB.move(-selectingAABB.minX, -selectingAABB.minY, -selectingAABB.minZ));
                } else selecting = null;
            }
        }
    }
    @SubscribeEvent
    public static void onLevelRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            if ((selected.left() && selected.right() != null)) {
                Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
                LevelRenderer.renderVoxelShape(event.getPoseStack(), consumer, selected.right(),
                        -cameraPos.x, -cameraPos.y, -cameraPos.z,
                        0.168F, 0.258F, 0.384F,
                        1F,
                        true);
            }
            if (selecting != null && selectingPos[0] != null && selectingPos[1] != null && selectingAABB != null) {
                Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());

                try {
                    LevelRenderer.renderVoxelShape(event.getPoseStack(), consumer, selecting,
                            selectingAABB.minX-cameraPos.x, selectingAABB.minY-cameraPos.y, selectingAABB.minZ-cameraPos.z,
                            0.901F, 0.760F, 0.709F,
                            1F,
                            true);

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }
    @SubscribeEvent
    public static void onDisconnected(ClientPlayerNetworkEvent.LoggingOut event) {
        new LambdaClientTaskInstance(20, (level -> {}), (renderTickEvent -> {}), ()-> {
            ClientProxy.isUpdatingNoCullingInfo = true;
            try {
                ClientProxy.chunksNoCullingBlocks.clear();
                ClientProxy.chunksNoCullingBlocks2.clear();
            } finally {
                ClientProxy.isUpdatingNoCullingInfo = false;
            }
        }).onAddedToWorld();
    }
}
