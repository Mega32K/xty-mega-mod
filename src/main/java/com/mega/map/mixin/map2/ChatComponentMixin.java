package com.mega.map.mixin.map2;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mega.map.common.data.map2.ClientGameData;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "intValue=40"))
    private int higherMessage(int original) {
        if (ClientGameData.map2Playing()) original = 70;
        return original;
    }
    @ModifyExpressionValue(method = "screenToChatY", at = @At(value = "CONSTANT", args = "doubleValue=40"))
    private double higherMessage(double original) {
        if (ClientGameData.map2Playing()) original = 70D;
        return original;
    }
}
