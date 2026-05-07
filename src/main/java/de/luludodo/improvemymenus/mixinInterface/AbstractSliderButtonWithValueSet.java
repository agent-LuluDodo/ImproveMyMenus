package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.client.OptionInstance;

public interface AbstractSliderButtonWithValueSet<T> {
    void improvemymenus$initValues(OptionInstance.SliderableValueSet<T> valueSet);
    OptionInstance.SliderableValueSet<T> improvemymenus$getValues();
}
