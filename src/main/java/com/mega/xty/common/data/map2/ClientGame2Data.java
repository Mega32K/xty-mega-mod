package com.mega.xty.common.data.map2;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.util.List;
import java.util.function.Consumer;

public class ClientGame2Data {
    public static List<PostRenderedItemRender> postRenderedItemRenders = new ObjectArrayList<>();
    public static List<ItemStack> extraWarehouseMeleeStacks = new ObjectArrayList<>();
    public static int tickCount = 0;
    public static boolean isStopped = true;

    public static void tick(ClientLevel clientLevel) {
        if (!ClientGameData.isStopped && !isStopped) {
            tickCount++;
        } else {
            tickCount = 0;
        }
    }
    public static boolean playing() {
        return !ClientGameData.isStopped && !isStopped;
    }
    public static void addPostRenderedItemRender(PoseStack poseStack, Consumer<PoseStack> consumer) {
        postRenderedItemRenders.add(new PostRenderedItemRender(poseStack, consumer));
    }
    public record PostRenderedItemRender(PoseStack poseStack, Consumer<PoseStack> consumer) {}
}
