package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.client.OptionInstance;

public interface IdentifiableOptionInstance {
    static void setIdentifier(OptionInstance<?> instance, Object identifier) {
        ((IdentifiableOptionInstance) (Object) instance).improvemymenus$setIdentifier(identifier);
    }
    static Object getIdentifier(OptionInstance<?> instance) {
        return ((IdentifiableOptionInstance) (Object) instance).improvemymenus$getIdentifier();
    }

    void improvemymenus$setIdentifier(Object identifier);
    Object improvemymenus$getIdentifier();
}
