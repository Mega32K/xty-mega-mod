package com.mega.xty.client.text;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ItemDisplayTooltip(ItemStack itemStack) implements TooltipComponent {
}
