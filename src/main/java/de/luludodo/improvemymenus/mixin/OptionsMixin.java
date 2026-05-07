package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.IdentifiableOptionInstance;
import de.luludodo.improvemymenus.util.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow
    @Final
    private OptionInstance<Integer> menuBackgroundBlurriness;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void improvemymenus$identify(Minecraft minecraft, File workingDirectory, CallbackInfo ci) {
        IdentifiableOptionInstance.setIdentifier(menuBackgroundBlurriness, Globals.MENU_BACKGROUND_BLUR_SLIDER_IDENTIFIER);
    }
}
