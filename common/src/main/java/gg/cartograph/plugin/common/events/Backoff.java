package gg.cartograph.plugin.common.events;

import gg.cartograph.plugin.common.config.BackoffConfig;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.function.LongSupplier;

/**
 * Tracks consecutive send failures and the next instant a send is allowed. Backoff
 * is independent of whether a batch is retained (that is the spool's job) — it only
 * rate-limits attempts. Not thread-safe; used only from the flush thread.
 */
public class Backoff
{
    private static final long BASE_SECONDS = 30;
    private static final int  MAX_SHIFT    = 20; // guards against long overflow

    private final BackoffConfig config;
    private final Random random;
    private final LongSupplier nanoClock;

    private int  consecutiveFailures = 0;
    private long nextAllowedNanos    = 0; // 0 == not blocked

    public Backoff(BackoffConfig config) { this(config, new Random(), System::nanoTime); }

    Backoff(BackoffConfig config, Random random, LongSupplier nanoClock)
    {
        this.config    = config;
        this.random    = random;
        this.nanoClock = nanoClock;
    }

    public boolean blocked() { return nextAllowedNanos != 0 && nanoClock.getAsLong() < nextAllowedNanos; }

    public void onSuccess() { consecutiveFailures = 0; nextAllowedNanos = 0; }

    public Duration onFailure(Optional<Duration> retryAfter)
    {
        consecutiveFailures++;
        long seconds;
        if (retryAfter.isPresent()) {
            seconds = Math.min(config.getRetryAfterCapSeconds(), Math.max(0, retryAfter.get().toSeconds()));
        } else {
            long exp = BASE_SECONDS << Math.min(consecutiveFailures - 1, MAX_SHIFT);
            seconds  = Math.min(config.getMaxSeconds(), exp);
            double jitter = 1.0 + ((random.nextDouble() * 0.4) - 0.2); // ±20%
            seconds = Math.max(1, (long) (seconds * jitter));
            seconds = Math.min(config.getMaxSeconds(), seconds);
        }
        Duration delay = Duration.ofSeconds(seconds);
        nextAllowedNanos = nanoClock.getAsLong() + delay.toNanos();
        return delay;
    }

    public int consecutiveFailures() { return consecutiveFailures; }
}
