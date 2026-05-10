package de.luludodo.improvemymenus.util;

import de.luludodo.improvemymenus.config.Config;
import net.minecraft.resources.Identifier;

public class DebugOptionsScreenUtil {

    public static String getTranslationKey(Identifier identifier) {
        StringBuilder builder = new StringBuilder("improvemymenus.");

        String namespace = identifier.getNamespace();
        if (!Identifier.DEFAULT_NAMESPACE.equals(namespace)) {
            builder.append(namespace).append(".");
        }

        builder.append("debug.options.").append(identifier.getPath());

        return builder.toString();
    }

    public static int leftAdjustment() {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS)
            return TextAdjustments.DEBUG_OPTION_ADJUSTMENT.left();
        return 0;
    }

    public static int centerAdjustment() {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS)
            return TextAdjustments.DEBUG_OPTION_ADJUSTMENT.center();
        return 0;
    }

    public static int rightAdjustment() {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS)
            return TextAdjustments.DEBUG_OPTION_ADJUSTMENT.right();
        return 0;
    }
}
