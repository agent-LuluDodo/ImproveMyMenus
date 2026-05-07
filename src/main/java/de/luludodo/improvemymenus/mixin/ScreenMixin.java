package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.util.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Inject(
            method = "extractBlurredBackground",
            at = @At("HEAD"),
            cancellable = true
    )
    private void improvemymenus$unblur(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (Config.Other.UNBLUR_VIDEO_SETTINGS && minecraft.level != null && (Object) this instanceof VideoSettingsScreen) {
            if (Globals.MENU_BACKGROUND_BLUR_SLIDER_HOVERED_OR_FOCUSED) {
                Globals.MENU_BACKGROUND_BLUR_SLIDER_HOVERED_OR_FOCUSED = false;
            } else {
                ci.cancel();
            }
        }
    }
}
