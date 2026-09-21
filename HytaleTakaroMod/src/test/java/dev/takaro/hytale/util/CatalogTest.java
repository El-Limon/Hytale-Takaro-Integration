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

    @Test
    void dropsScaffoldingRolesThatAreNotEntities() {
        assertTrue(Catalog.isPlaceholderRole("Static"));
        assertTrue(Catalog.isPlaceholderRole("Static2"));
        assertTrue(Catalog.isPlaceholderRole("Static4"));
        assertTrue(Catalog.isPlaceholderRole("Template"));
        assertTrue(Catalog.isPlaceholderRole("BlankTemplate"));
        assertTrue(Catalog.isPlaceholderRole("Empty_Role"));
        assertTrue(Catalog.isPlaceholderRole(null));
        assertFalse(Catalog.isPlaceholderRole("Bat_Ice"));
        assertFalse(Catalog.isPlaceholderRole("StaticTrork"));
    }

    @Test
    void buildsTheRoleTranslationKeyHytaleUses() {
        assertEquals("npcRoles.Bat_Ice.name", Catalog.roleNameKey("Bat_Ice"));
    }

    @Test
    void humanisesOnlyAsALastResort() {
        assertEquals("Rex Cave", Catalog.humanise("Rex_Cave"));
        assertEquals("Trork Grunt", Catalog.humanise("trork_grunt"));
        assertEquals("Bat", Catalog.humanise("Bat"));
        assertEquals("", Catalog.humanise(null));
    }
}
