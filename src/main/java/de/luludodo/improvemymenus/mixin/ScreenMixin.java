package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AbstractScrollAreaWithPageScrolling;
import de.luludodo.improvemymenus.mixinInterface.ScreenWithParent;
import de.luludodo.improvemymenus.util.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.input.KeyEvent;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ScreenWithParent {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    @Final
    private List<GuiEventListener> children;
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

    @ModifyReturnValue(
            method = "panoramaShouldSpin",
            at = @At("RETURN")
    )
    private boolean improvemymenus$panoramaShouldSpin(boolean original) {
        if (Config.Other.FIX_ONBOARDING_PANORAMA_SPINNING && (Object) this instanceof SoundOptionsScreen self) {
            return !(self.lastScreen instanceof AccessibilityOnboardingScreen);
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "keyPressed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/events/AbstractContainerEventHandler;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"
            )
    )
    private boolean improvemymenus$pageScroll(boolean original, KeyEvent event) {
        if (!Config.List.SUPPORT_PAGE_KEYS || original) return original;

        return switch (event.key()) {
            case GLFW.GLFW_KEY_PAGE_UP -> improvemymenus$scrollPage(-1);
            case GLFW.GLFW_KEY_PAGE_DOWN -> improvemymenus$scrollPage(1);
            default -> false;
        };
    }

    @Unique
    private boolean improvemymenus$scrollPage(int down) {
        AbstractScrollAreaWithPageScrolling found = null;
        for (GuiEventListener child : children) {
            if (child instanceof AbstractScrollAreaWithPageScrolling scrollable) {
                if (found != null) return false;
                found = scrollable;
            }
        }
        if (found == null) return false;
        found.improvemymenus$scrollPage(down);
        return true;
    }
}
