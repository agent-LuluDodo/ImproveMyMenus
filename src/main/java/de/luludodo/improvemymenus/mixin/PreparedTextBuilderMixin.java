package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.StyleWithOpacity;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Style.class)
public abstract class StyleMixin implements StyleWithOpacity {
    @Unique
    private int improvemymenus$opacity = 0xFF;

    @Override
    public void improvemymenus$setOpacity(int opacity) {
        this.improvemymenus$opacity = opacity;
    }

    @Override
    public int improvemymenus$getOpacity() {
        return improvemymenus$opacity;
    }
}
