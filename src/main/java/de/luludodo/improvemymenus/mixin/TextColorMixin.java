package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.TextColorWithAlpha;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextColor.class)
public abstract class TextColorMixin implements TextColorWithAlpha {
    @Unique
    private int improvemymenus$alpha = 0xFF;

    @Override
    public void improvemymenus$initAlpha(int alpha) {
        this.improvemymenus$alpha = alpha;
    }

    @Override
    public float improvemymenus$getAlpha() {
        return this.improvemymenus$alpha;
    }
}
