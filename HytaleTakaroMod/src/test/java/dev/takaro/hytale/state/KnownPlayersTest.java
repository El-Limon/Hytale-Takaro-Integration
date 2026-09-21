package dev.takaro.hytale.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KnownPlayersTest {

    private static final String ID = "73e84f0e-86c8-499d-8d31-b1d4cc32aedf";

    @Test
    void recordsAndReadsBackAPlayer(@TempDir Path dir) {
        KnownPlayers ledger = new KnownPlayers(dir.resolve("known-players.json"));
        ledger.record(ID, "GrandGrotto216", "hytale:" + ID, "192.168.129.50");

        assertEquals("GrandGrotto216", ledger.nameOf(ID));
        assertEquals(ID, ledger.getByName("grandgrotto216").gameId);
        assertEquals(1, ledger.size());
    }

    @Test
    void survivesAServerRestart(@TempDir Path dir) {
        Path file = dir.resolve("known-players.json");
        new KnownPlayers(file).record(ID, "GrandGrotto216", "hytale:" + ID, "192.168.129.50");

        KnownPlayers reloaded = new KnownPlayers(file);
        assertEquals("GrandGrotto216", reloaded.nameOf(ID));
        assertEquals("192.168.129.50", reloaded.get(ID).ip);
    }

    @Test
    void producesTheIGamePlayerShape(@TempDir Path dir) {
        KnownPlayers ledger = new KnownPlayers(dir.resolve("p.json"));
        ledger.record(ID, "GrandGrotto216", null, "10.0.0.1");

        Map<String, Object> player = ledger.toGamePlayer(ID);
        assertEquals(ID, player.get("gameId"));
        assertEquals("GrandGrotto216", player.get("name"));
        assertEquals("hytale:" + ID, player.get("platformId"));
        assertEquals("10.0.0.1", player.get("ip"));
    }

    @Test
    void unknownPlayerIsNullNotAFakeEntry(@TempDir Path dir) {
        KnownPlayers ledger = new KnownPlayers(dir.resolve("p.json"));
        assertNull(ledger.get("nope"));
        assertNull(ledger.nameOf("nope"));
        assertNull(ledger.toGamePlayer("nope"));
        assertNull(ledger.getByName("nobody"));
    }

    @Test
    void aCorruptLedgerDoesNotStopTheConnector(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("known-players.json");
        Files.writeString(file, "{ this is not json");

        KnownPlayers ledger = new KnownPlayers(file);
        assertEquals(0, ledger.size());
        ledger.record(ID, "Someone", null, null);
        assertEquals("Someone", ledger.nameOf(ID));
    }

    @Test
    void refreshingKeepsTheOldNameWhenTheNewOneIsMissing(@TempDir Path dir) {
        KnownPlayers ledger = new KnownPlayers(dir.resolve("p.json"));
        ledger.record(ID, "GrandGrotto216", null, "10.0.0.1");
        ledger.record(ID, null, null, null);
        assertEquals("GrandGrotto216", ledger.nameOf(ID));
        assertEquals("10.0.0.1", ledger.get(ID).ip);
    }

    @Test
    void recordWithoutAGameIdIsIgnored(@TempDir Path dir) {
        KnownPlayers ledger = new KnownPlayers(dir.resolve("p.json"));
        assertNull(ledger.record(null, "x", null, null));
        assertNull(ledger.record("", "x", null, null));
        assertEquals(0, ledger.size());
    }
}
