package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.OptionInstance$IntRangeBase")
public interface OptionInstanceIntRangeBaseMixin {
    @Shadow
    int maxInclusive();

    @Shadow
    int minInclusive();

    @Inject(
            method = "fromSliderValue(D)Ljava/lang/Integer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void improvemymenus$fromSpacing(double slider, CallbackInfoReturnable<Integer> cir) {
        if (Config.Slider.SPACING == Config.Slider.Spacing.VISUAL) {
            double clamped = Math.clamp(slider, 0d, 1d);
            double lengthMinus1 = maxInclusive() - minInclusive();
            int index = (int) Math.round(clamped * lengthMinus1);
            cir.setReturnValue(minInclusive() + index);
        }
    }

    @Inject(
            method = "toSliderValue(Ljava/lang/Integer;)D",
            at = @At("HEAD"),
            cancellable = true
    )
    private void improvemymenus$toSpacing(Integer value, CallbackInfoReturnable<Double> cir) {
        if (Config.Slider.SPACING == Config.Slider.Spacing.VISUAL || Config.Slider.SPACING == Config.Slider.Spacing.MIXED) {
            double index = value - minInclusive();
            double lengthMinus1 = maxInclusive() - minInclusive();
            cir.setReturnValue(index / lengthMinus1);
        }
    }
}
