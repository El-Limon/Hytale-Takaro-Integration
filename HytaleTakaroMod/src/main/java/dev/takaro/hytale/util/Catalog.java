package dev.takaro.hytale.util;

import java.util.Locale;

/**
 * Catalogue hygiene rules shared by listItems and listEntities.
 *
 * <p>No Hytale imports, so this is unit-testable without the server jar.
 */
public final class Catalog {
    private Catalog() {
    }

    /**
     * True for codes that are developer scaffolding rather than real game content, e.g.
     * {@code Debug_Donut_Selector}, {@code Test_Block}, {@code Dev_Marker} (F10).
     */
    public static boolean isDeveloperAsset(String code) {
        if (code == null) {
            return false;
        }
        String lower = code.toLowerCase(Locale.ROOT);
        return lower.startsWith("debug_") || lower.startsWith("test_") || lower.startsWith("dev_");
    }
}
