package de.luludodo.improvemymenus.util;

import de.luludodo.improvemymenus.config.Config;

public class SocialInteractionScreenUtil {
    public static int downAdjustment() {
        if (Config.Other.IMPROVE_SOCIAL_INTERACTIONS)
            return TextAdjustments.SOCIAL_INTERACTIONS_ADJUSTMENT.down();
        return 0;
    }
}
