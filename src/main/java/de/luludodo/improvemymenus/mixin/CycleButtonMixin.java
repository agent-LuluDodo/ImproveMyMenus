package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.*;
import de.luludodo.improvemymenus.util.DropdownOverlayScreen;
import de.luludodo.improvemymenus.util.Globals;
import de.luludodo.improvemymenus.util.IdentifierUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(CycleButton.class)
public abstract class CycleButtonMixin<T> extends AbstractButton implements CycleButtonWithIndicators, AbstractButtonWithType, CycleButtonWithDropdown {
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
    protected abstract void updateValue(T newValue);

    @Shadow
    private T value;
    @Shadow
    @Final
    private CycleButton.SpriteSupplier<T> spriteSupplier;
    @Shadow
    @Final
    private Function<T, Component> valueStringifier;
    @Shadow
    @Final
    private CycleButton.OnValueChange<T> onValueChange;
    @Unique
    private boolean improvemymenus$indicatorHovered = false;

    @Unique
    private int improvemymenus$hoveredIndex = 0;

    @Unique
    private Component improvemymenus$indicatorMessage = null;

    @Unique
    private final WidgetTooltipHolder improvemymenus$indicatorTooltip = new WidgetTooltipHolder();

    private CycleButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Unique private static final Identifier IMPROVEMYMENUS$INDICATOR =
            IdentifierUtil.id("cycle_button/indicator");
    @Unique private static final Identifier IMPROVEMYMENUS$INDICATOR_UNFOCUSED =
            IdentifierUtil.id("cycle_button/indicator_unfocused");
    @Unique private static final Identifier IMPROVEMYMENUS$INDICATOR_HIGHLIGHTED =
            IdentifierUtil.id("cycle_button/indicator_highlighted");
    @Unique private static final Identifier IMPROVEMYMENUS$INDICATOR_CURRENT =
            IdentifierUtil.id("cycle_button/indicator_current");
    @Unique private static final Identifier IMPROVEMYMENUS$INDICATOR_CURRENT_UNFOCUSED =
            IdentifierUtil.id("cycle_button/indicator_current_unfocused");
    @Unique private static final Identifier IMPROVEMYMENUS$INDICATOR_CURRENT_HIGHLIGHTED =
            IdentifierUtil.id("cycle_button/indicator_current_highlighted");

    @Inject(
            method = "extractContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CycleButton;extractDefaultSprite(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void improvemymenus$indicators(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (Globals.RELOAD_MESSAGES) {
            updateValue(value);
        }

        if (!improvemymenus$indicatorHovered)
            improvemymenus$hoveredIndex = -1;

        improvemymenus$indicatorHovered = false;
        if (this.improvemymenus$getType() != Type.NORMAL || !Config.CycleButton.INDICATORS) return;

        List<T> values = this.values.getSelectedList();

        boolean reverse = values.size() == 2 && Objects.equals(values.get(0), true) && Objects.equals(values.get(1), false);

        int count = values.size();
        int index = this.index;
        int width = this.getWidth() - 2;

        int elementWidth = width / (count + 1);
        int gap = Math.ceilDiv(elementWidth, 10);
        int rightGap = gap / 2;
        int leftGap = gap - rightGap;

        int indicatorWidth = elementWidth * count;

        int x = this.getX() + 1 + width / 2 - indicatorWidth / 2;
        int y = this.getY();
        int height = this.getHeight();
        int endY = y + height;
        int startY = endY - 5;

        int spriteWidth = elementWidth - leftGap - rightGap;

        boolean focused = isHoveredOrFocused();

        for (int i = 0; i < count; i++) {
            int startX = x + (reverse ? count - 1 - i : i) * elementWidth;
            int endX = startX + elementWidth;

            boolean hovered = this.active &&
                    focused &&
                    Config.CycleButton.INDICATOR != Config.ButtonBinding.UNBOUND &&
                    graphics.containsPointInScissor(mouseX, mouseY) &&
                    mouseX >= startX && mouseY >= startY &&
                    mouseX < endX && mouseY < endY;

            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    i == index
                            ? focused ? hovered ? IMPROVEMYMENUS$INDICATOR_CURRENT_HIGHLIGHTED : IMPROVEMYMENUS$INDICATOR_CURRENT : IMPROVEMYMENUS$INDICATOR_CURRENT_UNFOCUSED
                            : focused ? hovered ? IMPROVEMYMENUS$INDICATOR_HIGHLIGHTED         : IMPROVEMYMENUS$INDICATOR         : IMPROVEMYMENUS$INDICATOR_UNFOCUSED,
                    startX + leftGap,
                    y,
                    spriteWidth,
                    height,
                    ARGB.white(alpha)
            );

            if (hovered) {
                improvemymenus$indicatorHovered = true;
                if (improvemymenus$hoveredIndex != i) {
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
    }

    @Inject(
            method = "onPress",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void improvemymenus$onIndicatorPress(InputWithModifiers input, CallbackInfo ci) {
        if (improvemymenus$indicatorHovered && Config.CycleButton.INDICATOR.matches(input)) {
            cycleValue(improvemymenus$hoveredIndex - index);
            ci.cancel();
        } else if (Config.CycleButton.DROPDOWN.matches(input)) {
            improvemymenus$openDropdown();
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
        return Config.CycleButton.PREVIOUS.getModifier() == Config.Modifier.SHIFT ? original : Config.CycleButton.PREVIOUS.matches(input);
    }

    @Inject(
            method = "mouseScrolled",
            at = @At("HEAD"),
            cancellable = true
    )
    private void improvemymenus$scroll(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.CycleButton.SCROLL) cir.setReturnValue(false);
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
        return switch (Config.CycleButton.SHOW_ALTERNATIVES) {
            case NEVER -> false;
            case ALT -> original;
            case SHIFT -> Minecraft.getInstance().hasShiftDown();
            case CONTROL -> Minecraft.getInstance().hasControlDown();
            case ALWAYS -> true;
        };
    }

    @ModifyReturnValue(
            method = "onOffBuilder",
            at = @At("RETURN")
    )
    private static CycleButton.Builder<Boolean> improvemymenus$switch(CycleButton.Builder<Boolean> original) {
        CycleButtonBuilderWithType.setType(original, CycleButtonBuilderWithType.Type.ON_OFF_BUILDER);
        return original;
    }

    @ModifyExpressionValue(
            method = "createLabelForValue",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private <R> R improvemymenus$randomColor$0(R original) {
        return improvemymenus$randomColor(original);
    }

    @ModifyExpressionValue(
            method = "createFullName",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private <R> R improvemymenus$randomColor$1(R original) {
        return improvemymenus$randomColor(original);
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void improvemymenus$randomColor$2(int x, int y, int width, int height, Component message, Component name, int index, T value, Supplier<T> defaultValueSupplier, CycleButton.ValueListSupplier<T> values, Function<T, Component> valueStringifier, Function<CycleButton<T>, MutableComponent> narrationProvider, CycleButton.OnValueChange<T> onValueChange, OptionInstance.TooltipSupplier<T> tooltipSupplier, CycleButton.DisplayState displayState, CycleButton.SpriteSupplier<T> spriteSupplier, CallbackInfo ci) {
        updateValue(value);
    }

    @Unique
    private static <R> R improvemymenus$randomColor(R original) {
        if (!Config.CycleButton.RANDOM_COLORS || (Config.CycleButton.ON_OFF_COLORS && (original == CommonComponents.OPTION_ON || original == CommonComponents.OPTION_OFF))) {
            return original;
        } else if (original instanceof Component component) {
            int id;
            if (component.getContents() instanceof TranslatableContents translation) {
                id = translation.getKey().hashCode() * 17;
            } else {
                id = component.hashCode() * 17;
            }
            if (id < 0) id = -id;
            ChatFormatting color = switch(id % 8) {
                case 0 -> ChatFormatting.DARK_BLUE;
                case 1 -> ChatFormatting.DARK_AQUA;
                case 2 -> ChatFormatting.DARK_PURPLE;
                case 3 -> ChatFormatting.GOLD;
                case 4 -> ChatFormatting.BLUE;
                case 5 -> ChatFormatting.AQUA;
                case 6 -> ChatFormatting.LIGHT_PURPLE;
                case 7 -> ChatFormatting.YELLOW;
                // This should never happen, but it doesn't warrant a crash
                default -> ChatFormatting.WHITE;
            };
            //noinspection unchecked
            return (R) component.copy().withStyle(color);
        }
        return original;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void improvemymenus$openDropdown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            minecraft.setScreen(new DropdownOverlayScreen<>(
                    minecraft.screen,
                    (CycleButton<T>) (Object) this,
                    this.values,
                    this.spriteSupplier,
                    this.valueStringifier,
                    this.onValueChange
            ));
        }
    }
}
