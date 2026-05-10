package de.luludodo.improvemymenus.util;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DropdownOverlayScreen<T> extends Screen {
    private static final Component TITLE = Component.translatable("improvemymenus.dropdown.title");

    private final Screen parent;
    private final CycleButton<T> button;
    private final CycleButton.ValueListSupplier<T> values;
    private final CycleButton.SpriteSupplier<T> spriteSupplier;
    private final Function<T, Component> valueStringifier;
    private final CycleButton.OnValueChange<T> onValueChange;
    private final Map<T, Component> labelCache;
    private final T originalValue;

    public DropdownOverlayScreen(Screen parent, CycleButton<T> button, CycleButton.ValueListSupplier<T> values, CycleButton.SpriteSupplier<T> spriteSupplier, Function<T, Component> valueStringifier, CycleButton.OnValueChange<T> onValueChange) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font, TITLE);

        this.parent = parent;
        parent.clearFocus();

        this.button = button;
        this.values = values;
        this.spriteSupplier = spriteSupplier;
        this.valueStringifier = valueStringifier;
        this.onValueChange = onValueChange;
        this.labelCache = new HashMap<>();
        this.originalValue = button.getValue();
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        parent.extractBackground(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.button.setFocused(false);
        parent.extractRenderState(graphics, -1, -1, a);
        extractTransparentBackground(graphics);
        graphics.nextStratum();
        extractDropdown(graphics, mouseX, mouseY, a);
    }

    public void extractDropdown(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        T selectedValue = this.button.getValue();
        List<T> values = this.values.getSelectedList();
        int x = this.button.getX();
        int width = this.button.getWidth();
        int height = this.button.getHeight();
        int curY = this.button.getY();
        curY -= values.indexOf(selectedValue) * height;
        for (T value : values) {
            Component label;
            if (value.equals(selectedValue)) {
                label = this.button.getMessage();
            } else {
                label = this.labelCache.computeIfAbsent(value, this.valueStringifier);
            }
            boolean highlighted = mouseX >= x && mouseX < (x + width) && mouseY >= curY && mouseY < (curY + height);
            this.button.setFocused(highlighted);
            Identifier sprite = this.spriteSupplier.apply(this.button, value);
            if (sprite == null) {
                sprite = AbstractButton.SPRITES.get(true, highlighted);
            }

            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    sprite,
                    x,
                    curY,
                    width,
                    height
            );

            graphics.textRenderer().acceptScrollingWithDefaultCenter(
                    label,
                    x,
                    x + width,
                    curY,
                    curY + height
            );

            curY += height;
        }

        graphics.requestCursor(CursorTypes.POINTING_HAND);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (scrollY != 0) {
            List<T> values = this.values.getSelectedList();
            int newIndex = values.indexOf(this.button.getValue()) + (int) Math.signum(-scrollY);
            if (newIndex >= 0 && newIndex < values.size()) {
                T newValue = values.get(newIndex);
                this.button.setValue(newValue);
            }
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        int x = this.button.getX();
        int width = this.button.getWidth();

        if (mouseX >= x && mouseX < x + width) {

            T selectedValue = this.button.getValue();
            List<T> values = this.values.getSelectedList();
            int height = this.button.getHeight();
            int startY = this.button.getY();
            startY -= values.indexOf(selectedValue) * height;

            int index = (int) Math.floor((mouseY - startY) / height);
            if (index >= 0 && index < values.size()) {
                this.button.setValue(values.get(index));

                onClose();
                return true;
            }
        }

        this.button.setValue(this.originalValue);

        onClose();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            this.button.setValue(this.originalValue);

            onClose();
            return true;
        } else if (event.isDown()) {
            List<T> values = this.values.getSelectedList();
            int newIndex = values.indexOf(this.button.getValue()) + 1;
            if (newIndex < values.size()) {
                this.button.setValue(values.get(newIndex));
            }
        } else if (event.isUp()) {
            List<T> values = this.values.getSelectedList();
            int newIndex = values.indexOf(this.button.getValue()) - 1;
            if (newIndex >= 0) {
                this.button.setValue(values.get(newIndex));
            }
        } else if (event.isConfirmation()) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void resize(int width, int height) {
        parent.resize(width, height);
        super.resize(width, height);
    }

    @Override
    public void onClose() {
        this.parent.setFocused(this.button);
        this.minecraft.setScreen(this.parent);

        T value = this.button.getValue();
        if (!value.equals(originalValue)) {
            this.onValueChange.onValueChange(this.button, value);
        }
    }
}
