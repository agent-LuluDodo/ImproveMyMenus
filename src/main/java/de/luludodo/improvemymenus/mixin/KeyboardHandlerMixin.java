package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.ScreenWithParent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Definition(id = "minecraft", field = "Lnet/minecraft/client/KeyboardHandler;minecraft:Lnet/minecraft/client/Minecraft;")
    @Definition(id = "gui", field = "Lnet/minecraft/client/Minecraft;gui:Lnet/minecraft/client/gui/Gui;")
    @Definition(id = "screen", method = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;")
    @Expression("this.minecraft.gui.screen() != null")
    @ModifyExpressionValue(
            method = "handleDebugKeys",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private boolean improvemymenus$doNotCloseParent(boolean original) {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS) return false;
        return original;
    }

    @ModifyArg(
            method = "handleDebugKeys",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/screens/debug/DebugOptionsScreen;<init>()V"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    ordinal = 0
            )
    )
    private @Nullable Screen improvemymenus$parent(@Nullable Screen screen) {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS && screen instanceof DebugOptionsScreen && screen instanceof ScreenWithParent screenWithParent) {
            screenWithParent.improvemymenus$setParent(Minecraft.getInstance().gui.screen());
        }
        return screen;
    }
}
