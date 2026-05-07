package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.AbstractSliderButtonWithValueSet;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(OptionInstance.OptionInstanceSliderButton.class)
public abstract class OptionInstanceSliderButtonMixin<T> implements AbstractSliderButtonWithValueSet<T> {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void improvemymenus$init(Options options, int x, int y, int width, int height, OptionInstance<T> instance, OptionInstance.SliderableValueSet<T> values, OptionInstance.TooltipSupplier<T> tooltipSupplier, Consumer<T> onValueChanged, boolean applyValueImmediately, CallbackInfo ci) {
        improvemymenus$initValues(values);
    }
}
