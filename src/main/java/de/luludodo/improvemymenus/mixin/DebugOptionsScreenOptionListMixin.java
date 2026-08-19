package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.util.DebugOptionsScreenUtil;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(DebugOptionsScreen.OptionList.class)
public abstract class DebugOptionsScreenOptionListMixin {
    @Inject(
            method = "lambda$static$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/Identifier;compareTo(Lnet/minecraft/resources/Identifier;)I"
            ),
            cancellable = true
    )
    private static void improvemymenus$sort(Map.Entry<Identifier, DebugScreenEntry> o1, Map.Entry<Identifier, DebugScreenEntry> o2, CallbackInfoReturnable<Integer> cir) {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS) {
            String o1Text = I18n.get(DebugOptionsScreenUtil.getTranslationKey(o1.getKey()));
            String o2Text = I18n.get(DebugOptionsScreenUtil.getTranslationKey(o2.getKey()));
            cir.setReturnValue(o1Text.compareToIgnoreCase(o2Text));
        }
    }

    @ModifyExpressionValue(
            method = "updateSearch",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z"
            )
    )
    private static boolean improvemymenus$contains(boolean original, String value, @Local(name = "entry") Map.Entry<Identifier, DebugScreenEntry> entry) {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS && !original) {
            String text = I18n.get(DebugOptionsScreenUtil.getTranslationKey(entry.getKey()));
            return text.contains(value);
        }
        return original;
    }
}
