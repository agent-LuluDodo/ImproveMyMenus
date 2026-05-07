package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.network.chat.TextColor;

public interface TextColorWithAlpha {
    static TextColorWithAlpha cast(TextColor color) {
        return (TextColorWithAlpha) (Object) color;
    }

    static TextColor withAlpha(TextColor color, int alpha) {
        TextColor result = TextColor.fromRgb(color.getValue());
        cast(result).improvemymenus$initAlpha(alpha);
        return result;
    }

    static TextColor fromArgb(int argb) {
        TextColor result = TextColor.fromRgb(argb & 0xFFFFFF);
        cast(result).improvemymenus$initAlpha(argb >> 24 & 0xFF);
        return result;
    }

    static float getAlpha(TextColor color) {
        return cast(color).improvemymenus$getAlpha();
    }

    /**
     * <b>Don't use this method, as {@link TextColor} is supposed to be immutable!</b>
     * @deprecated This method isn't going anywhere, but you should use {@link #withAlpha(TextColor, int)} instead in 99.9% of cases
     * @see #withAlpha(TextColor, int)
     */
    @Deprecated
    void improvemymenus$initAlpha(int alpha);
    float improvemymenus$getAlpha();
}
