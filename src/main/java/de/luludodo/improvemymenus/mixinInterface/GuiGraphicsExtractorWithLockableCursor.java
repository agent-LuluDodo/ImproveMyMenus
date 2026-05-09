package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface GuiGraphicsExtractorWithLockableCursor {
    static void lockCursor(GuiGraphicsExtractor graphics) {
        ((GuiGraphicsExtractorWithLockableCursor) graphics).improvemymenus$lockCursor();
    }

    void improvemymenus$lockCursor();
}
