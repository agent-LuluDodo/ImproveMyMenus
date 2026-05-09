package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.ScreenWithParent;
import de.luludodo.improvemymenus.util.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ScreenWithParent {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Unique
    private Screen improvemymenus$parent = null;

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

    @Override
    public void improvemymenus$setParent(Screen parent) {
        this.improvemymenus$parent = parent;
    }

    @Override
    public Screen improvemymenus$getParent() {
        return improvemymenus$parent;
    }

    @ModifyArg(
            method = "onClose",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
            )
    )
    private Screen improvemymenus$parent(@Nullable Screen screen) {
        if (improvemymenus$parent != null)
            return improvemymenus$parent;
        return screen;
    }
}
