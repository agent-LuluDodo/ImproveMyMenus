package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {
    @Inject(
            method = "mouseScrolled",
            at = @At("HEAD"),
            cancellable = true
    )
    default void improvemymenus$zoom(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (Config.Other.ZOOM == Config.Other.ZoomOptions.EVERYWHERE && this instanceof Screen screen && Minecraft.getInstance().hasControlDown()) {
            OptionInstance<Integer> guiScale = Minecraft.getInstance().options.guiScale();
            if (guiScale.values() instanceof OptionInstance.ClampingLazyMaxIntRange range) {
                int old = guiScale.get();
                int adjustedOld = (old == 0 || old > range.maxInclusive()) ? range.maxInclusive() + 1 : old;
                int newValue = adjustedOld + (int) Math.signum(scrollY);
                System.out.println("old: " + old + " new: " + newValue +  " max: " + range.maxInclusive() + " min: " + range.minInclusive());
                if (newValue != 0 && newValue <= range.maxInclusive() && newValue >= range.minInclusive()) {
                    guiScale.set(newValue);
                    screen.resize(screen.width, screen.height);
                    cir.setReturnValue(true);
                }
            }
            cir.setReturnValue(false);
        }
    }
}
