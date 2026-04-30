package com.mega.xty.common.network.c2s.debug.fill_fucntion;

import com.mega.endinglib.api.item.component.ItemComponentManager;
import com.mega.xty.common.component.ComponentInit;
import com.mega.xty.common.item.FillFunctionCreatorItem;
import com.mega.xty.common.component.BlockLine;
import com.mega.xty.common.component.FillCreatorComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SFillMakeLinePacket {

    public C2SFillMakeLinePacket() {
    }
    public static C2SFillMakeLinePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SFillMakeLinePacket();
    }

    public static void encode(C2SFillMakeLinePacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(C2SFillMakeLinePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SFillMakeLinePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item = itemStack.getItem();
        if (item instanceof FillFunctionCreatorItem ffc) {
            CompoundTag nbt = itemStack.getTag();
            if (nbt != null) {
                CompoundTag interaction = nbt.getCompound(FillFunctionCreatorItem.INTERACTION);
                if (!interaction.isEmpty()) {
                    BlockPos start = FillFunctionCreatorItem.fromArray(interaction.getIntArray("0"));
                    BlockPos end = FillFunctionCreatorItem.fromArray(interaction.getIntArray("1"));
                    if (start != null && end != null) {
                        FillCreatorComponent component = ItemComponentManager.get(itemStack, ComponentInit.FILL_CREATOR);
                        if (component != null)
                            component.addLine(itemStack, new BlockLine(start, end));
                    }
                }
            }
        }
    }
}
