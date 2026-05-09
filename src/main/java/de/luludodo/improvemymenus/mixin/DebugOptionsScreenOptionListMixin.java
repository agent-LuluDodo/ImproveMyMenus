package de.luludodo.improvemymenus.mixin;

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
}
