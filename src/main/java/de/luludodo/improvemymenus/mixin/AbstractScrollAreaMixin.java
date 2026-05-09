package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.GuiGraphicsExtractorWithLockableCursor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractScrollArea.class)
public abstract class AbstractScrollAreaMixin {
    @Shadow
    public abstract int maxScrollAmount();

    @Shadow
    private boolean scrolling;

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

    @Unique
    private boolean improvemymenus$lockCursor = false;

    @ModifyExpressionValue(
            method = "extractScrollbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/AbstractScrollArea;isOverScrollbar(DD)Z",
                    ordinal = 1
            )
    )
    private boolean improvemymenus$forceCursor(boolean original) {
        if (Config.List.FORCE_CURSOR && scrolling) {
            improvemymenus$lockCursor = true;
            return true;
        }
        return original;
    }

    @Inject(
            method = "extractScrollbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;requestCursor(Lcom/mojang/blaze3d/platform/cursor/CursorType;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void improvemymenus$lockCursor(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (improvemymenus$lockCursor) {
            improvemymenus$lockCursor = false;
            GuiGraphicsExtractorWithLockableCursor.lockCursor(graphics);
        }
    }
}
