package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AbstractScrollAreaWithPageScrolling;
import de.luludodo.improvemymenus.mixinInterface.GuiGraphicsExtractorWithLockableCursor;
import de.luludodo.improvemymenus.util.ScheduleUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractScrollArea.class)
public abstract class AbstractScrollAreaMixin extends AbstractWidget implements AbstractScrollAreaWithPageScrolling {
    private AbstractScrollAreaMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Shadow
    public abstract int maxScrollAmount();

    @Shadow
    private boolean scrolling;

    @Shadow
    protected abstract int scrollBarX();

    @Shadow
    public abstract int scrollbarWidth();

    @Shadow
    public abstract void setScrollAmount(double scrollAmount);

    @Shadow
    public abstract double scrollAmount();

    @Shadow
    protected abstract int scrollerHeight();

    @Shadow
    public abstract int scrollBarY();

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

    @ModifyReturnValue(
            method = "isOverScrollbar",
            at = @At("RETURN")
    )
    private boolean improvemymenus$accurateScrollbarHovering(boolean original, double x, double y) {
        return original && (!Config.List.ACCURATE_HOVERING || x < scrollBarX() + scrollbarWidth());
    }

    @Override
    public void improvemymenus$scrollPage(int down) {
        setScrollAmount(scrollAmount() + Math.max(0, getHeight() - Config.List.PAGE_OVERLAP) * down);
    }

    @Unique
    private double improvemymenus$pageScrollY = 0;

    @Inject(
            method = "updateScrolling",
            at = @At("RETURN")
    )
    private void improvemymenus$updateScrolling(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!this.scrolling && Config.List.SCROLLBAR_ON_CLICK != Config.List.ScrollbarOnClick.VANILLA) return;

        switch (Config.List.SCROLLBAR_ON_CLICK) {
            case JUMP -> {
                if (isOverHandle(event.y())) return;

                double scrollerHeight = scrollerHeight();
                double startY = getY() + scrollerHeight / 2d;
                double height = getHeight() - scrollerHeight;

                setScrollAmount(maxScrollAmount() * (event.y() - startY) / height);
            }
            case PAGE -> {
                this.improvemymenus$pageScrollY = event.y();
                if (improvemymenus$tickPageScroll(Util.getMillis()))
                    this.scrolling = false;
            }
        }
    }

    @Inject(
            method = "onRelease",
            at = @At("HEAD")
    )
    private void improvemymenus$onRelease(MouseButtonEvent event, CallbackInfo ci) {
        ScheduleUtil.clear(this);
    }

    @Unique
    private boolean improvemymenus$tickPageScroll(long time) {
        if (isOverHandle(improvemymenus$pageScrollY)) return false;

        improvemymenus$scrollPage(improvemymenus$pageScrollY < scrollBarY() ? -1 : 1);

        ScheduleUtil.set(this, time + 250, this::improvemymenus$tickPageScroll);
        return true;
    }

    @Unique
    private boolean isOverHandle(double y) {
        double scrollerY = scrollBarY();
        double scrollerHeight = scrollerHeight();
        return y >= scrollerY && y < scrollerY + scrollerHeight;
    }

    @ModifyExpressionValue(
            method = "mouseDragged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/input/MouseButtonEvent;y()D"
            )
    )
    private double improvemymenus$mouseDragged(double original) {
        return Config.List.SCROLLBAR_ON_CLICK == Config.List.ScrollbarOnClick.VANILLA ? original : getY();
    }
}
