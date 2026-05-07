package de.luludodo.improvemymenus.util;

import de.luludodo.improvemymenus.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public class CommonComponentsUtil {
    private static MutableComponent ORIGINAL_ON;
    private static MutableComponent ORIGINAL_OFF;

    public static void setOriginalOn(MutableComponent original) {
        ORIGINAL_ON = original;
    }
    public static void setOriginalOff(MutableComponent original) {
        ORIGINAL_OFF = original;
    }

    public static MutableComponent getOn() {
        if (Config.CycleButton.ON_OFF_COLORS && ORIGINAL_ON != null) {
            return ORIGINAL_ON.copy().withStyle(ChatFormatting.GREEN);
        } else {
            return ORIGINAL_ON;
        }
    }

    public static MutableComponent getOff() {
        if (Config.CycleButton.ON_OFF_COLORS && ORIGINAL_OFF != null) {
            return ORIGINAL_OFF.copy().withStyle(ChatFormatting.RED);
        } else {
            return ORIGINAL_OFF;
        }
    }
}
