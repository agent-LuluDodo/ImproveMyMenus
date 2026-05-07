package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.luludodo.improvemymenus.mixinInterface.CycleButtonBuilderWithType;
import de.luludodo.improvemymenus.mixinInterface.AbstractButtonWithType;
import net.minecraft.client.gui.components.CycleButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CycleButton.Builder.class)
public abstract class CycleButtonBuilderMixin<T> implements CycleButtonBuilderWithType {
    @Shadow
    private CycleButton.DisplayState displayState;

    @Unique
    private Type improvemymenus$type = Type.NORMAL;

    @Unique
    private boolean improvemymenus$defaultSpriteSupplier = true;

    @Inject(
            method = "withSprite",
            at = @At("HEAD")
    )
    private void improvemymenus$customSpriteSupplier(CycleButton.SpriteSupplier<T> spriteSupplier, CallbackInfoReturnable<CycleButton.Builder<T>> cir) {
        improvemymenus$defaultSpriteSupplier = false;
    }

    @ModifyReturnValue(
            method = "create(IIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/CycleButton$OnValueChange;)Lnet/minecraft/client/gui/components/CycleButton;",
            at = @At(
                    value = "RETURN"
            )
    )
    private CycleButton<T> improvemymenus$applyType(CycleButton<T> self) {
        if (improvemymenus$type == Type.ON_OFF_BUILDER &&
                displayState == CycleButton.DisplayState.VALUE &&
                improvemymenus$defaultSpriteSupplier) {
            ((AbstractButtonWithType) self).improvemymenus$setType(AbstractButtonWithType.Type.ON_OFF);
        }
        return self;
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
