package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.AdvancementsScreenWithTickDelta;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin implements AdvancementsScreenWithTickDelta {
    @Unique
    private float improvemymenus$tickDelta = 0f;

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;extractTooltips(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V"
            )
    )
    private void improvemymenus$updateTickDelta(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        improvemymenus$tickDelta = a;
    }

    @Override
    public float improvemymenus$getTickDelta() {
        return improvemymenus$tickDelta;
    }
}
