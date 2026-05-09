package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.GuiGraphicsExtractorWithLockableCursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin implements GuiGraphicsExtractorWithLockableCursor {

    @Unique
    private boolean improvemymenus$cursor_locked;

    @Inject(
            method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/state/gui/GuiRenderState;II)V",
            at = @At("RETURN")
    )
    private void improvemymenus$init(Minecraft minecraft, GuiRenderState guiRenderState, int mouseX, int mouseY, CallbackInfo ci) {
        improvemymenus$cursor_locked = false;
    }

    @Inject(
            method = "requestCursor",
            at = @At("HEAD"),
            cancellable = true
    )
    private void requestCursor(CallbackInfo ci) {
        if (improvemymenus$cursor_locked)
            ci.cancel();
    }

    @Override
    public void improvemymenus$lockCursor() {
        improvemymenus$cursor_locked = true;
    }
}
