package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.client.gui.components.AbstractButton;

public interface AbstractButtonWithType {
    static void setType(AbstractButton button, Type type) {
        ((AbstractButtonWithType) button).improvemymenus$setType(type);
    }

    static Type getType(AbstractButton button) {
        return ((AbstractButtonWithType) button).improvemymenus$getType();
    }

    enum Type {
        ON_OFF,
        NORMAL,
        DEBUG_OPTION_LEFT,
        DEBUG_OPTION_CENTER,
        DEBUG_OPTION_RIGHT,
        SOCIAL_INTERACTIONS_TAB,
        SOCIAL_INTERACTIONS_TAB_SELECTED,
    }

    void improvemymenus$setType(Type type);
    Type improvemymenus$getType();
}
