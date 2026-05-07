package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.client.gui.components.AbstractSliderButton;

public interface IdentifiableAbstractSliderButton {
    static void setIdentifier(AbstractSliderButton slider, Object identifier) {
        ((IdentifiableAbstractSliderButton) (Object) slider).improvemymenus$setIdentifier(identifier);
    }
    static Object getIdentifier(AbstractSliderButton slider) {
        return ((IdentifiableAbstractSliderButton) (Object) slider).improvemymenus$getIdentifier();
    }

    void improvemymenus$setIdentifier(Object identifier);
    Object improvemymenus$getIdentifier();
}
