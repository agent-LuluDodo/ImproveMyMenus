package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.luludodo.improvemymenus.config.Config;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin {
    @ModifyExpressionValue(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;hasControlDown()Z"
            )
    )
    private boolean improvemymenus$zoom(boolean original) {
        return original && Config.Other.ZOOM != Config.Other.ZoomOptions.NOWHERE;
    }

    @ModifyExpressionValue(
            method = "mouseScrolled",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/Options;guiScale()Lnet/minecraft/client/OptionInstance;",
                            ordinal = 0
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
                    ordinal = 0
            )
    )
    private Object improvemymenus$zoomClampGuiScale(Object original, @Local(name = "clampingLazyMaxIntRange") OptionInstance.ClampingLazyMaxIntRange range) {
        if (original instanceof Integer integer && integer > range.maxInclusive()) {
            return range.maxInclusive() + 1;
        }
        return original;
    }
}
