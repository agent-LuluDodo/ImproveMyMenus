package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.network.chat.Style;

public interface StyleWithOpacity {
    static StyleWithOpacity cast(Style style) {
        return (StyleWithOpacity) (Object) style;
    }

    static Style withOpacity(Style style, float opacity) {
        return cast(style).improvemymenus$withOpacity(opacity);
    }

    static float getOpacity(Style style) {
        return cast(style).improvemymenus$getOpacity();
    }

    Style improvemymenus$withOpacity(float opacity);
    float improvemymenus$getOpacity();
}
