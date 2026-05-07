package de.luludodo.improvemymenus.util;

import net.minecraft.resources.Identifier;

import static net.minecraft.resources.Identifier.fromNamespaceAndPath;

public class IdentifierUtil {
    public static final String NAMESPACE = Globals.MOD_ID;

    public static Identifier id(String path) {
        return fromNamespaceAndPath(NAMESPACE, path);
    }
}
