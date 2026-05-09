package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.client.gui.components.CycleButton;

public interface CycleButtonBuilderWithType {
    static void setType(CycleButton.Builder<?> builder, Type type) {
        ((CycleButtonBuilderWithType) builder).improvemymenus$setType(type);
    }

    static Type getType(CycleButton.Builder<?> builder) {
        return ((CycleButtonBuilderWithType) builder).improvemymenus$getType();
    }

    enum Type {
        ON_OFF_BUILDER,
        NORMAL
    }

    void improvemymenus$setType(Type type);
    Type improvemymenus$getType();
}
