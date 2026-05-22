package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.luludodo.improvemymenus.config.Config;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TextCursorUtils;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {
    @Shadow
    private long focusedTime;

    @Unique
    private long improvemymenus$lastChange;

    @Unique
    private boolean improvemymenus$cursorVisibleInitialized = false;

    @ModifyArg(
            method = "extractWidgetRenderState",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;min(II)I",
                    ordinal = 0
            ),
            index = 0
    )
    private int improvemymenus$textHighlightStart(int original, @Local(name = "cursorX") int cursorX, @Local(name = "highlightX") int highlightX) {
        return Config.EditBox.CONSISTENT_HIGHLIGHTING ? Math.min(cursorX, highlightX) : original;
    }

    @ModifyArg(
            method = "extractWidgetRenderState",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;min(II)I",
                    ordinal = 1
            ),
            index = 0
    )
    private int improvemymenus$textHighlightEnd(int original, @Local(name = "cursorX") int cursorX, @Local(name = "highlightX") int highlightX, @Local(name = "insert") boolean insert) {
        return Config.EditBox.CONSISTENT_HIGHLIGHTING ? Math.max(insert ? cursorX : cursorX - 1, highlightX) : original;
    }

    @Inject(
            method = "onValueChange",
            at = @At("HEAD")
    )
    private void improvemymenus$onValueChange(CallbackInfo ci) {
        improvemymenus$lastChange = Util.getMillis();
    }

    @Inject(
            method = "setHighlightPos",
            at = @At("HEAD")
    )
    private void improvemymenus$setHighlightPos(int pos, CallbackInfo ci) {
        improvemymenus$lastChange = Util.getMillis();
    }

    @Inject(
            method = "setCursorPosition",
            at = @At("HEAD")
    )
    private void improvemymenus$setCursorPosition(int pos, CallbackInfo ci) {
        improvemymenus$lastChange = Util.getMillis();
    }

    @ModifyExpressionValue(
            method = "extractWidgetRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/TextCursorUtils;isCursorVisible(J)Z"
            )
    )
    private boolean improvemymenus$isCursorVisible(boolean original) {
        return switch (Config.EditBox.SOLID_CURSOR) {
            case ALWAYS -> true;
            case WHILE_TYPING -> {
                if (!improvemymenus$cursorVisibleInitialized) {
                    improvemymenus$cursorVisibleInitialized = true;
                    improvemymenus$lastChange = focusedTime;
                }

                yield (Util.getMillis() - improvemymenus$lastChange) / Config.EditBox.BLINK_INTERVAL % 2 == 0;
            }
            case NEVER -> Config.EditBox.BLINK_INTERVAL == TextCursorUtils.CURSOR_BLINK_INTERVAL_MS ?
                    original :
                    (Util.getMillis() - focusedTime) / Config.EditBox.BLINK_INTERVAL % 2 == 0;
        };
    }
}
