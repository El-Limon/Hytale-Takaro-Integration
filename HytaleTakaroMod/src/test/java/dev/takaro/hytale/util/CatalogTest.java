package dev.takaro.hytale.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CatalogTest {

    @Test
    void developerScaffoldingIsRecognised() {
        assertTrue(Catalog.isDeveloperAsset("Debug_Donut_Selector"));
        assertTrue(Catalog.isDeveloperAsset("Debug_Forking_2"));
        assertTrue(Catalog.isDeveloperAsset("Test_Block"));
        assertTrue(Catalog.isDeveloperAsset("Dev_Marker"));
        assertTrue(Catalog.isDeveloperAsset("debug_lowercase"));
    }

    @Test
    void realContentIsKept() {
        assertFalse(Catalog.isDeveloperAsset("Armor_Adamantite_Chest"));
        assertFalse(Catalog.isDeveloperAsset("Wood_Oak_Trunk"));
        // Only a prefix counts - a real item that merely contains the word stays in.
        assertFalse(Catalog.isDeveloperAsset("Tool_Debugger"));
        assertFalse(Catalog.isDeveloperAsset("Device_Panel"));
        assertFalse(Catalog.isDeveloperAsset(null));
        assertFalse(Catalog.isDeveloperAsset(""));
    }
}
