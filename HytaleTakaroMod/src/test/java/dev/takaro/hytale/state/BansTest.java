package dev.takaro.hytale.state;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BansTest {

    @Test
    void expiresAtParsesIsoInstants() {
        assertEquals(Instant.parse("2026-12-31T23:59:59Z"), Bans.parseExpiresAt("2026-12-31T23:59:59Z"));
        assertEquals(Instant.parse("2026-09-21T18:00:00Z"), Bans.parseExpiresAt("2026-09-21T20:00:00+02:00"));
    }

    @Test
    void absentOrBlankExpiryMeansPermanent() {
        assertNull(Bans.parseExpiresAt(null));
        assertNull(Bans.parseExpiresAt(""));
        assertNull(Bans.parseExpiresAt("   "));
        assertFalse(Bans.isUnparseableExpiry(null));
        assertFalse(Bans.isUnparseableExpiry(""));
    }

    @Test
    void garbageExpiryIsReportedRatherThanSilentlyIgnored() {
        assertNull(Bans.parseExpiresAt("tomorrow"));
        assertTrue(Bans.isUnparseableExpiry("tomorrow"));
    }

    @Test
    void iBanShapeMatchesWhatTakaroExpects() {
        Map<String, Object> ban = Bans.toIBan(
            "73e84f0e-86c8-499d-8d31-b1d4cc32aedf", "GrandGrotto216", "griefing",
            Instant.parse("2026-12-31T23:59:59Z"));

        @SuppressWarnings("unchecked")
        Map<String, Object> player = (Map<String, Object>) ban.get("player");
        assertEquals("73e84f0e-86c8-499d-8d31-b1d4cc32aedf", player.get("gameId"));
        assertEquals("GrandGrotto216", player.get("name"));
        assertEquals("hytale:73e84f0e-86c8-499d-8d31-b1d4cc32aedf", player.get("platformId"));
        assertEquals("griefing", ban.get("reason"));
        assertEquals("2026-12-31T23:59:59Z", ban.get("expiresAt"));
    }

    @Test
    void permanentBanHasNullExpiresAtAndUnknownNameStaysNull() {
        Map<String, Object> ban = Bans.toIBan("aaaa0000-0000-0000-0000-000000000001", null, null, null);
        assertTrue(ban.containsKey("expiresAt"));
        assertNull(ban.get("expiresAt"));
        assertNull(ban.get("reason"));
        @SuppressWarnings("unchecked")
        Map<String, Object> player = (Map<String, Object>) ban.get("player");
        assertNull(player.get("name"));
    }
}
