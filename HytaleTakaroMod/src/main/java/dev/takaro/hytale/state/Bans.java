package dev.takaro.hytale.state;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure mapping helpers between Hytale's ban model and Takaro's {@code IBan} shape.
 *
 * <p>Takaro expects {@code {player: {gameId, name, platformId}, reason, expiresAt}}, where
 * {@code expiresAt} is an ISO-8601 instant or null for a permanent ban.
 *
 * <p>No Hytale imports, so this is unit-testable without the server jar.
 */
public final class Bans {
    private Bans() {
    }

    /** Parse Takaro's optional expiresAt. Returns null for null / blank / unparseable input. */
    public static Instant parseExpiresAt(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(trimmed);
        } catch (DateTimeParseException e) {
            try {
                // Tolerate an offset form such as 2026-09-21T20:00:00+02:00
                return java.time.OffsetDateTime.parse(trimmed).toInstant();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    /** True when the string was non-blank but could not be parsed as a timestamp. */
    public static boolean isUnparseableExpiry(String value) {
        return value != null && !value.trim().isEmpty() && parseExpiresAt(value) == null;
    }

    /**
     * Build one row of Takaro's listBans response.
     *
     * @param gameId    banned player's game id (UUID string), never null
     * @param name      last known name, or null if the server has never seen the player
     * @param reason    ban reason, or null
     * @param expiresOn expiry instant, or null for a permanent ban
     */
    public static Map<String, Object> toIBan(String gameId, String name, String reason, Instant expiresOn) {
        Map<String, Object> player = new HashMap<>();
        player.put("gameId", gameId);
        player.put("name", name);
        player.put("platformId", "hytale:" + gameId);

        Map<String, Object> ban = new HashMap<>();
        ban.put("player", player);
        ban.put("reason", reason);
        ban.put("expiresAt", expiresOn == null ? null : expiresOn.toString());
        return ban;
    }
}
