package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.config.Config;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(OptionInstance.SliderableEnum.class)
public abstract class OptionInstanceSliderableEnumMixin<T> {
    @Shadow
    @Final
    private List<T> values;

    @Inject(
            method = "fromSliderValue",
            at = @At("HEAD"),
            cancellable = true
    )
    private void improvemymenus$fromSpacing(double slider, CallbackInfoReturnable<T> cir) {
        if (Config.Slider.ENUM_SPACING == Config.Slider.Spacing.VISUAL) {
            double clamped = Math.clamp(slider, 0d, 1d);
            int index = (int) Math.round(clamped * (values.size() - 1));
            cir.setReturnValue(values.get(index));
        }
    }

    @Inject(
            method = "toSliderValue",
            at = @At("HEAD"),
            cancellable = true
    )
    private void improvemymenus$toSpacing(T value, CallbackInfoReturnable<Double> cir) {
        if (Config.Slider.ENUM_SPACING == Config.Slider.Spacing.AREA) {
            int index = values.indexOf(value);
            if (index == 0) {
                cir.setReturnValue(0d);
            } else if (index == (values.size() - 1)) {
                cir.setReturnValue(1d);
            } else {
                cir.setReturnValue((index + 0.5d) / values.size());
            }
        }
    }
}
