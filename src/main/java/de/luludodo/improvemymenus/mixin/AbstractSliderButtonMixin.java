package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AbstractSliderButtonWithValueSet;
import de.luludodo.improvemymenus.mixinInterface.IdentifiableAbstractSliderButton;
import de.luludodo.improvemymenus.util.Globals;
import de.luludodo.improvemymenus.util.IdentifierUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(AbstractSliderButton.class)
public abstract class AbstractSliderButtonMixin<T> extends AbstractWidget implements AbstractSliderButtonWithValueSet<T>, IdentifiableAbstractSliderButton {

    private AbstractSliderButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Unique
    private OptionInstance.SliderableValueSet<T> improvemymenus$valueSet;

    @Unique
    private float[] improvemymenus$valueLocations;

    @Unique
    private boolean improvemymenus$showSeparators = false;

    @Unique private static final Identifier IMPROVEMYMENUS$SEPARATOR =
            IdentifierUtil.id("slider/separator");
    @Unique private static final Identifier IMPROVEMYMENUS$SEPARATOR_HIGHLIGHTED =
            IdentifierUtil.id("slider/separator_highlighted");

    @Unique
    private Object improvemymenus$identifier = null;

    @Inject(
            method = "extractWidgetRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V",
                    ordinal = 1
            )
    )
    private void improvemymenus$separators(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (Config.Other.UNBLUR_VIDEO_SETTINGS && improvemymenus$identifier == Globals.MENU_BACKGROUND_BLUR_SLIDER_IDENTIFIER)
            Globals.MENU_BACKGROUND_BLUR_SLIDER_HOVERED_OR_FOCUSED = isHoveredOrFocused();

        if (!improvemymenus$showSeparators || !Config.Slider.SEPARATORS) return;

        int x = getX() + 4;
        int y = getY();
        int width = getWidth() - 8;
        int height = getHeight();

        float preview = -1;
        if (Config.Slider.HIGHLIGHT && isHovered) {
            Minecraft mc = Minecraft.getInstance();
            // the mouseX passed to this function isn't accurate enough, since we need sub-pixel accuracy
            double accurateMouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());

            T temp = this.improvemymenus$valueSet.fromSliderValue((accurateMouseX - x) / (double) width);
            preview = (float) this.improvemymenus$valueSet.toSliderValue(temp);
        }

        for (float location : this.improvemymenus$valueLocations) {
            if (location < 0.001f || location > 0.999f) continue;
            int curX = x + Math.round(width * location - 1.5f);
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    Math.abs(location - preview) < 0.001f ? IMPROVEMYMENUS$SEPARATOR_HIGHLIGHTED : IMPROVEMYMENUS$SEPARATOR,
                    curX,
                    y,
                    3,
                    height
            );
        }
    }

    @Override
    public void improvemymenus$initValues(OptionInstance.SliderableValueSet<T> valueSet) {
        this.improvemymenus$valueSet = valueSet;
        List<T> values = new ArrayList<>();
        T prev;
        T cur = valueSet.fromSliderValue(0d);
        int i = 0;
        while (i < 10) {
            values.add(cur);
            Optional<T> next = valueSet.next(cur).flatMap(valueSet::validateValue);
            if (next.isEmpty()) break;
            prev = cur;
            cur = next.get();
            if (prev == cur) break;
            i++;
        }
        if (i < 10) {
            improvemymenus$showSeparators = true;
            int length = values.size();
            this.improvemymenus$valueLocations = new float[length];
            for (int j = 0; j < length; j++) {
                this.improvemymenus$valueLocations[j] = (float) valueSet.toSliderValue(values.get(j));
            }
        }
    }

    @Override
    public OptionInstance.SliderableValueSet<T> improvemymenus$getValues() {
        return this.improvemymenus$valueSet;
    }

    @Override
    public void improvemymenus$setIdentifier(Object identifier) {
        this.improvemymenus$identifier = identifier;
    }

    @Override
    public Object improvemymenus$getIdentifier() {
        return this.improvemymenus$identifier;
    }
}
