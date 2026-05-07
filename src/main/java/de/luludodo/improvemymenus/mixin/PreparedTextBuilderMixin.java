package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.luludodo.improvemymenus.mixinInterface.TextColorWithAlpha;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public abstract class PreparedTextBuilderMixin {
    @ModifyExpressionValue(
            method = "getTextColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ARGB;alpha(I)I"
            )
    )
    private int improvemymenus$applyAlpha(int original, TextColor textColor) {
        return Math.clamp(Math.round((float) original * (TextColorWithAlpha.getAlpha(textColor) / 255f)), 0, 0xFF);
    }
}
