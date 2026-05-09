package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.client.gui.screens.Screen;

public interface ScreenWithParent {
    void improvemymenus$setParent(Screen parent);
    Screen improvemymenus$getParent();
}
