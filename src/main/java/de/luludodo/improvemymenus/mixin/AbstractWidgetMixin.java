package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AbstractButtonWithType;
import de.luludodo.improvemymenus.mixinInterface.AbstractWidgetWithTooltipGetter;
import de.luludodo.improvemymenus.mixinInterface.CycleButtonWithIndicators;
import de.luludodo.improvemymenus.util.DebugOptionsScreenUtil;
import de.luludodo.improvemymenus.util.SocialInteractionScreenUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements AbstractWidgetWithTooltipGetter {
    @Shadow
    @Final
    private WidgetTooltipHolder tooltip;

    @Override
    public Tooltip improvemymenus$getTooltip() {
        return this.tooltip.get();
    }

    @ModifyReceiver(
            method = "extractTooltipForNextRenderPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/WidgetTooltipHolder;refreshTooltipForNextRenderPass(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIZZLnet/minecraft/client/gui/navigation/ScreenRectangle;)V"
            )
    )
    private WidgetTooltipHolder improvemymenus$indicatorTooltip(WidgetTooltipHolder original, GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered, boolean isFocused, ScreenRectangle screenRectangle) {
        if (this instanceof CycleButtonWithIndicators self && self.improvemymenus$isIndicatorHovered()) {
            return self.improvemymenus$getIndicatorTooltip();
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/AbstractWidget;isValidClickButton(Lnet/minecraft/client/input/MouseButtonInfo;)Z"
            )
    )
    private boolean improvemymenus$isValidClickButton(boolean original, MouseButtonEvent event) {
        if (this instanceof CycleButtonWithIndicators self) {
            if ((!Config.CycleButton.INDICATORS || Config.CycleButton.INDICATOR == Config.ButtonBinding.LEFT) &&
                    Config.CycleButton.DROPDOWN == Config.ButtonBinding.UNBOUND &&
                    Config.CycleButton.NEXT == Config.ButtonBinding.LEFT &&
                    Config.CycleButton.PREVIOUS == Config.ButtonBinding.SHIFT_LEFT)
                return original;
            return Config.CycleButton.DROPDOWN.matches(event) ||
                    Config.CycleButton.NEXT.matches(event) ||
                    Config.CycleButton.PREVIOUS.matches(event) ||
                    (Config.CycleButton.INDICATORS && self.improvemymenus$isIndicatorHovered() && Config.CycleButton.INDICATOR.matches(event));
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "extractScrollingStringOverContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/AbstractWidget;getX()I"
            )
    )
    private int improvemymenus$adjustTextX(int original) {
        if (this instanceof AbstractButtonWithType buttonWithType) {
            if (Config.Other.IMPROVE_DEBUG_OPTIONS) {
                switch (buttonWithType.improvemymenus$getType()) {
                    case DEBUG_OPTION_LEFT -> {
                        return original + DebugOptionsScreenUtil.leftAdjustment();
                    }
                    case DEBUG_OPTION_CENTER -> {
                        return original + DebugOptionsScreenUtil.centerAdjustment();
                    }
                    case DEBUG_OPTION_RIGHT -> {
                        return original + DebugOptionsScreenUtil.rightAdjustment();
                    }
                }
            }
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "extractScrollingStringOverContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/AbstractWidget;getY()I"
            )
    )
    private int improvemymenus$adjustTextY(int original) {
        if (this instanceof AbstractButtonWithType buttonWithType) {
            if (Config.Other.IMPROVE_SOCIAL_INTERACTIONS) {
                switch (buttonWithType.improvemymenus$getType()) {
                    case SOCIAL_INTERACTIONS_TAB, SOCIAL_INTERACTIONS_TAB_SELECTED -> {
                        return original + SocialInteractionScreenUtil.downAdjustment();
                    }
                }
            }
        }
        return original;
    }
}
