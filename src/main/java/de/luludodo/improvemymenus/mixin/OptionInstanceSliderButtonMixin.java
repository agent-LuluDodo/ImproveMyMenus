package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AbstractSliderButtonWithValueSet;
import de.luludodo.improvemymenus.mixinInterface.IdentifiableAbstractSliderButton;
import de.luludodo.improvemymenus.mixinInterface.IdentifiableOptionInstance;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(OptionInstance.OptionInstanceSliderButton.class)
public abstract class OptionInstanceSliderButtonMixin<T> implements AbstractSliderButtonWithValueSet<T>, IdentifiableAbstractSliderButton {
    @Shadow
    public abstract void resetValue();

    @Shadow
    public abstract void applyUnsavedValue();

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void improvemymenus$init(Options options, int x, int y, int width, int height, OptionInstance<T> instance, OptionInstance.SliderableValueSet<T> values, OptionInstance.TooltipSupplier<T> tooltipSupplier, Consumer<T> onValueChanged, boolean applyValueImmediately, CallbackInfo ci) {
        improvemymenus$setIdentifier(IdentifiableOptionInstance.getIdentifier(instance));
        improvemymenus$initValues(values);
    }

    @Inject(
            method = "applyValue",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/OptionInstance$OptionInstanceSliderButton;delayedApplyAt:Ljava/lang/Long;",
                    opcode = Opcodes.PUTFIELD
            ),
            cancellable = true
    )
    private void improvemymenus$snap$1(CallbackInfo ci) {
        if (Config.Slider.SNAP == Config.Slider.SnapTiming.ON_RELEASE)
            ci.cancel();
    }

    @ModifyExpressionValue(
            method = "applyValue",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/OptionInstance$OptionInstanceSliderButton;applyValueImmediately:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean improvemymenus$snap$2(boolean original) {
        return original || Config.Slider.SNAP == Config.Slider.SnapTiming.WHILE_DRAGGING;
    }

    @Inject(
            method = "applyValue",
            at = @At("RETURN")
    )
    private void improvemymenus$snap$3(CallbackInfo ci) {
        if (Config.Slider.SNAP == Config.Slider.SnapTiming.WHILE_DRAGGING)
            this.resetValue();
    }

    @ModifyExpressionValue(
            method = "onRelease",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/OptionInstance$OptionInstanceSliderButton;applyValueImmediately:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean improvemymenus$snap$4(boolean original) {
        return switch (Config.Slider.SNAP) {
            case VANILLA -> original;
            case ON_RELEASE -> {
                if (!original) {
                    this.applyUnsavedValue();
                }
                yield true;
            }
            case WHILE_DRAGGING -> false;
        };
    }
}
