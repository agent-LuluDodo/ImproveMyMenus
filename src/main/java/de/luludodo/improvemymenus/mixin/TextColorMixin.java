package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.StyleWithOpacity;
import net.minecraft.network.chat.*;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.Optional;

@Mixin(Style.class)
public abstract class StyleMixin implements StyleWithOpacity {
    @Shadow
    private static <T> Style checkEmptyAfterChange(Style newStyle, @Nullable T previous, @Nullable T next) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Unique
    private float improvemymenus$opacity = 1f;

    

    @Override
    public Style improvemymenus$withOpacity(float opacity) {
        this.improvemymenus$opacity = opacity;

        return

        return Objects.equals(this.improvemymenus$opacity, opacity)
                ? this
                : create()
        )
    }

    @Override
    public float improvemymenus$getOpacity() {
        return improvemymenus$opacity;
    }
}
