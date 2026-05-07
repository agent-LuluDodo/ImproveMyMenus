package de.luludodo.improvemymenus.mixinInterface;

public interface AbstractButtonWithType {
    enum Type {
        NORMAL,
        ON_OFF
    }

    void improvemymenus$setType(Type type);
    Type improvemymenus$getType();
}
