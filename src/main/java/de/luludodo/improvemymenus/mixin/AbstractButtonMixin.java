package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AbstractButtonWithType;
import de.luludodo.improvemymenus.mixinInterface.CycleButtonWithIndicators;
import de.luludodo.improvemymenus.util.IdentifierUtil;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin implements AbstractButtonWithType {

    @Unique
    private Type improvemymenus$type;

    @Unique
    private static final Component EMPTY = Component.empty();

    @Inject(
            method = "<init>",
            at = @At("CTOR_HEAD")
    )
    private void improvemymenus$init(int x, int y, int width, int height, Component message, CallbackInfo ci) {
        improvemymenus$type = Type.NORMAL;
    }

    @ModifyArg(
            method = "extractDefaultLabel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/AbstractButton;extractScrollingStringOverContents(Lnet/minecraft/client/gui/ActiveTextCollector;Lnet/minecraft/network/chat/Component;I)V"
            )
    )
    private Component improvemymenus$indicatorMessage(Component original) {
        if (Config.CycleButton.SWITCHES && improvemymenus$type == Type.ON_OFF) {
            return EMPTY;
        } else {
            return (this instanceof CycleButtonWithIndicators self && self.improvemymenus$isIndicatorHovered())
                    ? self.improvemymenus$getIndicatorMessage()
                    : original;
        }
    }

    @Unique
    private static final WidgetSprites IMPROVEMYMENUS$ON_BUTTON = new WidgetSprites(
            IdentifierUtil.id("on_off_button/button_on"),
            IdentifierUtil.id("on_off_button/button_on_disabled"),
            IdentifierUtil.id("on_off_button/button_on_highlighted")
    );

    @Unique
    private static final WidgetSprites IMPROVEMYMENUS$OFF_BUTTON = new WidgetSprites(
            IdentifierUtil.id("on_off_button/button_off"),
            IdentifierUtil.id("on_off_button/button_off_disabled"),
            IdentifierUtil.id("on_off_button/button_off_highlighted")
    );

    @ModifyReceiver(
            method = "extractDefaultSprite",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/WidgetSprites;get(ZZ)Lnet/minecraft/resources/Identifier;"
            )
    )
    private WidgetSprites improvemymenus$modifySprite(WidgetSprites original, boolean enabled, boolean focused) {
        if (Config.CycleButton.SWITCHES && improvemymenus$type == Type.ON_OFF) {
            //noinspection unchecked, DataFlowIssue
            return ((CycleButton<Boolean>) (Object) this).getValue() ?
                    IMPROVEMYMENUS$ON_BUTTON : IMPROVEMYMENUS$OFF_BUTTON;
        } else {
            return original;
        }
    }

    @Override
    public Type improvemymenus$getType() {
        return this.improvemymenus$type;
    }

    @Override
    public void improvemymenus$setType(Type type) {
        this.improvemymenus$type = type;
    }
}