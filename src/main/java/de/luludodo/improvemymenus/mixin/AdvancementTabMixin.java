package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AdvancementsScreenWithTickDelta;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin {
    @Shadow
    private float fade;

    @Shadow
    @Final
    private AdvancementsScreen screen;

    @ModifyArg(
            method = "extractTooltips",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F",
                    ordinal = 0
            ),
            index = 0
    )
    private float improvemymenus$fadeIn(float original) {
        if (Config.Other.INDEPENDENT_ANIMATIONS && screen instanceof AdvancementsScreenWithTickDelta tickDelta) {
            return fade + 0.06F * tickDelta.improvemymenus$getTickDelta();
        }
        return original;
    }

    @ModifyArg(
            method = "extractTooltips",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F",
                    ordinal = 1
            ),
            index = 0
    )
    private float improvemymenus$fadeOut(float original) {
        if (Config.Other.INDEPENDENT_ANIMATIONS && screen instanceof AdvancementsScreenWithTickDelta tickDelta) {
            return fade - 0.12F * tickDelta.improvemymenus$getTickDelta();
        }
        return original;
    }
}
