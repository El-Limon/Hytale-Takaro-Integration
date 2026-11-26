package dev.takaro.hytale.util;

/**
 * Token bucket for {@code log} game events.
 *
 * <p>Takaro rate-limits each server's events (seen on staging 2026-11-26: 100 per 5 s sustained,
 * 600 per 30 s burst) and drops the excess with an {@code event-rate-limited} row. Every boot
 * of a Hytale server writes about 600 log lines before the plugin identifies; flushed in one go
 * they lost 20 lines per restart, and live forwarding at 50 per 2 s was over the sustained limit
 * too. Log events therefore pass through this bucket; other event types never wait.
 *
 * <p>No Hytale imports, so this is unit-testable without the server jar. Not thread-safe:
 * callers synchronise.
 */
public final class LogPacer {
    private final double capacity;
    private final double perMs;
    private double tokens;
    private long last;

    /**
     * @param perSecond sustained rate
     * @param burst     bucket size (also the start level)
     * @param nowMs     clock reading at construction
     */
    public LogPacer(double perSecond, int burst, long nowMs) {
        this.capacity = burst;
        this.perMs = perSecond / 1000.0;
        this.tokens = burst;
        this.last = nowMs;
    }

    /** Take one token if there is one. */
    public boolean tryAcquire(long nowMs) {
        refill(nowMs);
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill(long nowMs) {
        if (nowMs > last) {
            tokens = Math.min(capacity, tokens + (nowMs - last) * perMs);
            last = nowMs;
        }
    }
}
