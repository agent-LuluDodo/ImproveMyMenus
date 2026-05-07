package de.luludodo.improvemymenus.mixinInterface;

public interface CycleButtonBuilderWithType {
    enum Type {
        ON_OFF_BUILDER,
        NORMAL
    }

    void improvemymenus$setType(Type type);
    Type improvemymenus$getType();
}
