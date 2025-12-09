package com.mega.xty.common.network.c2s.debug.fill_fucntion;

import com.mega.endinglib.api.item.component.ItemComponentManager;
import com.mega.xty.common.item.FillFunctionCreatorItem;
import com.mega.xty.common.item.component.FillCreatorComponent;
import com.mega.xty.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SClearRecordsPacket {

    public C2SClearRecordsPacket() {
    }
    public static C2SClearRecordsPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new C2SClearRecordsPacket();
    }

    public static void encode(C2SClearRecordsPacket packet, FriendlyByteBuf friendlyByteBuf) {
    }

    public static void handle(C2SClearRecordsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (packet != null)
                handle0(packet, context);
        });
        context.get().setPacketHandled(true);
    }

    static void handle0(C2SClearRecordsPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item = itemStack.getItem();
        if (item instanceof FillFunctionCreatorItem ffc) {
            FillCreatorComponent component = ItemComponentManager.get(itemStack, CommonProxy.FILL_CREATOR);
            if (component != null) {
                component.clearRecords(itemStack, player);
            }
        }
    }
}
