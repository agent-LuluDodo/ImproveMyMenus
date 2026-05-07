package de.luludodo.improvemymenus.mixinInterface;

public interface CycleButtonWithType {
    enum Type {
        NORMAL,
        SWITCH
    }

    void improvemymenus$setType(Type type);
    Type improvemymenus$getType();
}
