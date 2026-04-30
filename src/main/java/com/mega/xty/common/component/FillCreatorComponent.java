package com.mega.xty.common.component;

import com.mega.endinglib.api.item.component.ComponentChanges;
import com.mega.endinglib.api.item.component.ItemComponentManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record FillCreatorComponent(List<BlockLine> records, List<BlockLine> history, Optional<Block> use) {
    public static final Codec<FillCreatorComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockLine.CODEC.listOf().optionalFieldOf("records", List.of()).forGetter(FillCreatorComponent::records),
                    BlockLine.CODEC.listOf().optionalFieldOf("history", List.of()).forGetter(FillCreatorComponent::history),
                    BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("use").forGetter(FillCreatorComponent::use)
                    ).apply(instance, FillCreatorComponent::new)
    );
    public void addLine(ItemStack stack, BlockLine line) {
        ItemComponentManager manager = ItemComponentManager.get(stack);
        List<BlockLine> records = new ObjectArrayList<>(this.records);
        List<BlockLine> history = new ObjectArrayList<>(this.history);
        records.add(line);
        history.add(line);
        manager.mergeChangedToNBTAndUpdate(ComponentChanges
                .builder(stack.getItem())
                .add(ComponentInit.FILL_CREATOR, new FillCreatorComponent(records, history, this.use))
                .build());
    }
    public void undo(ItemStack stack, Player player) {
        ItemComponentManager manager = ItemComponentManager.get(stack);
        if (!this.records.isEmpty())  {
            List<BlockLine> records = new ObjectArrayList<>(this.records);
            records.remove(records.size()-1);
            manager.mergeChangedToNBTAndUpdate(ComponentChanges
                    .builder(stack.getItem())
                    .add(ComponentInit.FILL_CREATOR, new FillCreatorComponent(records, this.history, this.use))
                    .build());
        }
    }
    public void redo(ItemStack stack, Player player) {
        ItemComponentManager manager = ItemComponentManager.get(stack);
        if (this.records.size() < this.history.size()) {
            List<BlockLine> records = new ObjectArrayList<>(this.records);
            records.add(this.history.get(records.size()));
            manager.mergeChangedToNBTAndUpdate(ComponentChanges
                    .builder(stack.getItem())
                    .add(ComponentInit.FILL_CREATOR, new FillCreatorComponent(records, this.history, this.use))
                    .build());
        }
    }
    public void setUseBlock(ItemStack stack, Block block) {
        ItemComponentManager manager = ItemComponentManager.get(stack);
        if (this.use.isEmpty() || !Objects.equals(this.use.get(), block)) {
            manager.mergeChangedToNBTAndUpdate(ComponentChanges
                    .builder(stack.getItem())
                    .add(ComponentInit.FILL_CREATOR, new FillCreatorComponent(records, this.history, Optional.of(block)))
                    .build());
        }
    }
    public void clearRecords(ItemStack stack, Player player) {
        ItemComponentManager manager = ItemComponentManager.get(stack);
        manager.mergeChangedToNBTAndUpdate(ComponentChanges
                .builder(stack.getItem())
                .add(ComponentInit.FILL_CREATOR, new FillCreatorComponent(List.of(), this.history, this.use))
                .build());

    }
}
