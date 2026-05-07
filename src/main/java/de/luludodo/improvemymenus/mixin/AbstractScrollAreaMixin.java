package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.config.Config;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractScrollArea.class)
public abstract class AbstractScrollAreaMixin {
    @Shadow
    public abstract int maxScrollAmount();

    @Inject(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/AbstractScrollArea;setScrollAmount(D)V"
            ),
            cancellable = true
    )
    private void improvemymenus$mouseScrolled(double mx, double my, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof AbstractSelectionList<?> self)) return;

        if (switch (Config.List.SCROLL_BEHAVIOUR) {
            case ONLY_PARENT -> false;
            case PREFER_PARENT -> maxScrollAmount() == 0 && self.getChildAt(mx, my)
                    .filter(child -> child.mouseScrolled(mx, my, scrollX, scrollY)).isPresent();
            case PREFER_CHILDREN -> self.getChildAt(mx, my)
                    .filter(child -> child.mouseScrolled(mx, my, scrollX, scrollY)).isPresent();
        }) cir.setReturnValue(true);
    }
}
