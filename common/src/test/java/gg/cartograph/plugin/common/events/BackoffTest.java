package gg.cartograph.plugin.common.events;

import gg.cartograph.plugin.common.config.BackoffConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class BackoffTest
{
    private final AtomicLong nanos = new AtomicLong(0);

    private Backoff newBackoff() {
        // Random(0): first nextDouble() ~0.730; used only to prove jitter stays in-range.
        return new Backoff(new BackoffConfig(), new Random(0), nanos::get);
    }

    @Test void startsUnblocked() {
        assertFalse(newBackoff().blocked());
    }

    @Test void exponentialGrowthWithinCap() {
        var b = newBackoff();
        long d1 = b.onFailure(Optional.empty()).toSeconds();  // ~30s ±20%
        b.onSuccess();
        var b2 = newBackoff();
        b2.onFailure(Optional.empty());
        long d2 = b2.onFailure(Optional.empty()).toSeconds();  // ~60s ±20%
        assertTrue(d1 >= 24 && d1 <= 36, "first delay in [24,36] was " + d1);
        assertTrue(d2 >= 48 && d2 <= 72, "second delay in [48,72] was " + d2);
    }

    @Test void capsExponentialAtMaxSeconds() {
        var b = newBackoff();
        Duration last = Duration.ZERO;
        for (int i = 0; i < 20; i++) last = b.onFailure(Optional.empty());
        assertTrue(last.toSeconds() <= 900, "capped at 900 was " + last.toSeconds());
    }

    @Test void retryAfterOverridesAndIsCapped() {
        var b = newBackoff();
        assertEquals(120, b.onFailure(Optional.of(Duration.ofSeconds(120))).toSeconds());
        assertEquals(3600, b.onFailure(Optional.of(Duration.ofSeconds(99999))).toSeconds());
    }

    @Test void blockedUntilDeadlineThenClears() {
        var b = newBackoff();
        var delay = b.onFailure(Optional.of(Duration.ofSeconds(10)));
        assertTrue(b.blocked());
        nanos.addAndGet(delay.toNanos() - 1);
        assertTrue(b.blocked());
        nanos.addAndGet(2);
        assertFalse(b.blocked());
    }

    @Test void successResets() {
        var b = newBackoff();
        b.onFailure(Optional.empty());
        b.onSuccess();
        assertFalse(b.blocked());
        assertEquals(0, b.consecutiveFailures());
    }
}
