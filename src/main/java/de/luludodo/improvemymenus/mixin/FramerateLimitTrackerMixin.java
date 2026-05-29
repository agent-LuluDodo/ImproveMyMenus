package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import de.luludodo.improvemymenus.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FramerateLimitTracker.class)
public abstract class FramerateLimitTrackerMixin {
    @Shadow
    public abstract FramerateLimitTracker.FramerateThrottleReason getThrottleReason();

    @ModifyReturnValue(
            method = "getFramerateLimit",
            at = @At("RETURN")
    )
    private int improvemymenus$menuFps(int original) {
        if (Config.Other.MENU_FPS != 60 && getThrottleReason() == FramerateLimitTracker.FramerateThrottleReason.OUT_OF_LEVEL_MENU) {
            return Config.Other.MENU_FPS;
        }
        return original;
    }
}
