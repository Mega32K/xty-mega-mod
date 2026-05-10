package com.mega.xty.common.data.map2;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mega.xty.common.options.map2game2.Game2ServerOptions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.util.List;
import java.util.function.Consumer;

public class ClientGame2Data {
    public static final Game2ServerOptions SERVER_OPTIONS = new Game2ServerOptions();
    public static List<PostRenderedItemRender> postRenderedItemRenders = new ObjectArrayList<>();
    public static List<ItemStack> extraWarehouseMeleeStacks = new ObjectArrayList<>();
    public static int tickCount = 0;
    public static boolean isStopped = true;
    public static int roundStartLockedTicks;

    public static void tick(ClientLevel clientLevel) {
        if (!ClientGameData.isStopped && !isStopped) {
            tickCount++;
            if (roundStartLockedTicks > 0) {
                roundStartLockedTicks--;
            }
        } else {
            tickCount = 0;
            roundStartLockedTicks = 0;
        }
    }
    public static boolean playing() {
        return !ClientGameData.isStopped && !isStopped;
    }
    public static boolean isRoundStartLocked() {
        return roundStartLockedTicks > 0;
    }
    public static void setRoundStartLockedTicks(int roundStartLockedTicks) {
        ClientGame2Data.roundStartLockedTicks = Math.max(0, roundStartLockedTicks);
    }
    public static void addPostRenderedItemRender(PoseStack poseStack, Consumer<PoseStack> consumer) {
        postRenderedItemRenders.add(new PostRenderedItemRender(poseStack, consumer));
    }
    public record PostRenderedItemRender(PoseStack poseStack, Consumer<PoseStack> consumer) {}
}
