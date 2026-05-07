package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AbstractWidgetWithTooltipGetter;
import de.luludodo.improvemymenus.mixinInterface.CycleButtonWithIndicators;
import de.luludodo.improvemymenus.mixinInterface.CycleButtonWithType;
import de.luludodo.improvemymenus.mixinInterface.TextColorWithAlpha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CycleButton.class)
public abstract class CycleButtonMixin<T> extends AbstractButton implements CycleButtonWithIndicators, CycleButtonWithType {
    @Shadow
    @Final
    private CycleButton.ValueListSupplier<T> values;

    @Shadow
    private int index;

    @Shadow
    protected abstract Component createLabelForValue(T newValue);

    @Shadow
    protected abstract void cycleValue(int delta);

    @Shadow
    @Final
    private OptionInstance.TooltipSupplier<T> tooltipSupplier;

    @Shadow
    public abstract T getValue();

    @Unique
    private boolean improvemymenus$indicatorHovered = false;

    @Unique
    private int improvemymenus$hoveredIndex = 0;

    @Unique
    private Component improvemymenus$indicatorMessage = null;

    @Unique
    private final WidgetTooltipHolder improvemymenus$indicatorTooltip = new WidgetTooltipHolder();

    @Unique
    private Type improvemymenus$type = Type.NORMAL;

    private CycleButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Unique
    private boolean improvemymenus$renderNormal = true;

    @Inject(
            method = "extractContents",
            at = @At("HEAD"),
            cancellable = true
    )
    private void improvemymenus$renderStart(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        switch (this.improvemymenus$type) {
            case SWITCH -> {
                if (!Config.CycleButton.SWITCHES) return;
                improvemymenus$renderNormal = false;

                Identifier sprite = AbstractButton.SPRITES.get(true, this.overrideRenderHighlightedSprite != null ? this.overrideRenderHighlightedSprite.get() : this.isHoveredOrFocused());
                boolean value = (boolean) getValue();
                if (getWidth() >= getHeight()) {
                    graphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            sprite,
                            getX() + (value ? getWidth() - getHeight() : 0),
                            getY(),
                            getHeight(),
                            getHeight(),
                            ARGB.multiplyAlpha(value ? 0x00FF00 : 0xFF0000, alpha)
                    );
                    graphics.enableScissor(
                            getX() + (value ? 0 : getHeight()),
                            getY(),
                            getX() + getWidth() - (value ? getHeight() : 0),
                            getY() + getHeight()
                    );
                } else {
                    graphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            sprite,
                            getX(),
                            getY() + (value ? 0 : getHeight() - getWidth()),
                            getWidth(),
                            getWidth(),
                            ARGB.multiplyAlpha(value ? 0x00FF00 : 0xFF0000, alpha)
                    );
                    graphics.enableScissor(
                            getX(),
                            getY() + (value ? getWidth() : 0),
                            getX() + getWidth(),
                            getY() + getHeight() - (value ? 0 : getWidth())
                    );
                }
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        AbstractButton.SPRITES.disabled(),
                        getX(),
                        getY(),
                        getWidth(),
                        getHeight(),
                        ARGB.white(alpha)
                );
                graphics.disableScissor();

                ci.cancel();
            }
        }
    }

    @Inject(
            method = "extractContents",
            at = @At("RETURN")
    )
    private void improvemymenus$renderEnd(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!improvemymenus$renderNormal || !Config.CycleButton.INDICATORS) return;

        List<T> values = this.values.getSelectedList();
        int count = values.size();
        int index = this.index;
        int width = this.getWidth() - 2;

        int elementWidth = width / (count + 1);
        int gap = Math.max(elementWidth / 10, 1);
        int leftGap = gap / 2;
        int rightGap = gap - leftGap;

        int x = this.getX() + 1 + elementWidth / 2 + rightGap;
        int startY = this.getY() + this.getHeight() - 3;
        int endY = startY + 2;

        improvemymenus$indicatorHovered = false;
        for (int i = 0; i < count; i++) {
            int startX = x + i * elementWidth;
            int endX = startX + elementWidth;

            boolean hovered = Config.CycleButton.CLICKABLE_INDICATORS &&
                    graphics.containsPointInScissor(mouseX, mouseY) &&
                    mouseX >= startX && mouseY >= (startY - 1) &&
                    mouseX < endX && mouseY <= endY;

            graphics.fill(startX + leftGap, hovered ? startY : startY + 1, endX - rightGap, endY, i == index ? 0xFFFFFFFF : 0x77FFFFFF);

            if (hovered) {
                improvemymenus$indicatorHovered = true;
                improvemymenus$hoveredIndex = i;
                if (i == index) {
                    improvemymenus$indicatorMessage = message;
                    improvemymenus$indicatorTooltip.set(((AbstractWidgetWithTooltipGetter) this).improvemymenus$getTooltip());
                } else {
                    T value = values.get(i);
                    MutableComponent message = createLabelForValue(value).copy();
                    Style style = message.getStyle();
                    TextColor color = style.getColor();
                    if (color == null) {
                        improvemymenus$indicatorMessage = message.withStyle(
                                style.withColor(TextColorWithAlpha.fromArgb(0xAAFFFFFF))
                        );
                    } else {
                        improvemymenus$indicatorMessage = message.withStyle(
                                style.withColor(TextColorWithAlpha.withAlpha(color, 0xAA))
                        );
                    }
                    improvemymenus$indicatorTooltip.set(tooltipSupplier.apply(value));
                }
            }
        }
    }

    @Inject(
            method = "onPress",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void improvemymenus$onIndicatorPress(InputWithModifiers input, CallbackInfo ci) {
        if (improvemymenus$indicatorHovered) {
            cycleValue(improvemymenus$hoveredIndex - index);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "onPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/input/InputWithModifiers;hasShiftDown()Z"
            )
    )
    private boolean improvemymenus$inputModifier(boolean original, InputWithModifiers input) {
        boolean result = switch (Config.CycleButton.PREVIOUS) {
            case NONE -> false;
            case ALT -> input.hasAltDown();
            case SHIFT -> original;
            case CONTROL -> input.hasControlDown();
        };
        return Config.CycleButton.INVERT ? !result : result;
    }

    @ModifyArg(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CycleButton;cycleValue(I)V"
            )
    )
    private int improvemymenus$scroll(int original) {
        return Config.CycleButton.SCROLL ? original : 0;
    }

    @Override
    public boolean improvemymenus$isIndicatorHovered() {
        return improvemymenus$indicatorHovered;
    }

    @Override
    public Component improvemymenus$getIndicatorMessage() {
        return improvemymenus$indicatorMessage;
    }

    @Override
    public WidgetTooltipHolder improvemymenus$getIndicatorTooltip() {
        return improvemymenus$indicatorTooltip;
    }

    @ModifyReturnValue(
            method = "lambda$static$0",
            at = @At("RETURN")
    )
    private static boolean improvemymenus$alternativeList(boolean original) {
        return switch (Config.CycleButton.ALTERNATIVE_LIST) {
            case NEVER -> false;
            case ALT -> original;
            case SHIFT -> Minecraft.getInstance().hasShiftDown();
            case CONTROL -> Minecraft.getInstance().hasControlDown();
            case ALWAYS -> true;
        };
    }

    @Override
    public void improvemymenus$setType(Type type) {
        this.improvemymenus$type = type;
    }

    @Override
    public Type improvemymenus$getType() {
        return this.improvemymenus$type;
    }
}
