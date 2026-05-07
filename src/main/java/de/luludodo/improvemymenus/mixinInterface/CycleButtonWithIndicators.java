package de.luludodo.improvemymenus.mixinInterface;

import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.network.chat.Component;

public interface CycleButtonWithIndicators {
    boolean improvemymenus$isIndicatorHovered();
    Component improvemymenus$getIndicatorMessage();
    WidgetTooltipHolder improvemymenus$getIndicatorTooltip();
}
