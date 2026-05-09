package de.luludodo.improvemymenus.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface DummyEntryUtil {
    interface DummyEntry {}

    static boolean isDummy(Object o) {
        return o instanceof DummyEntry;
    }

    DummyDebugOptionsScreenEntry DEBUG_OPTIONS_SCREEN = new DummyDebugOptionsScreenEntry();

    class DummyDebugOptionsScreenEntry extends DebugOptionsScreen.AbstractOptionEntry implements DummyEntry {
        private DummyDebugOptionsScreenEntry() {}

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            throw new NotImplementedException();
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            throw new NotImplementedException();
        }

        @Override
        public void refreshEntry() {
            throw new NotImplementedException();
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            throw new NotImplementedException();
        }
    }
}
