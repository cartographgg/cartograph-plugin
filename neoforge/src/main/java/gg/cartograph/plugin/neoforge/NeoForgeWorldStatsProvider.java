package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.world.WorldStatsProvider;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;

import java.util.concurrent.atomic.AtomicReference;

/**
 * NeoForge world stats holder.
 *
 * <p>Version-neutral: it owns the rate-limit and the latest-snapshot reference,
 * but the actual {@code net.minecraft} sampling (iterating levels, counting
 * chunks, reading dimension ids) lives in the per-version
 * {@link NeoForgePlatform#sampleWorldStats(Object)} impl.</p>
 *
 * <p>Sampling is driven by {@link CartographNeoForgeMod#onServerTick} on the
 * server tick thread; the heartbeat reads {@link #snapshot()} from any thread.</p>
 */
public class NeoForgeWorldStatsProvider implements WorldStatsProvider
{

    private final AtomicReference<WorldStatsSnapshot> latest = new AtomicReference<>(WorldStatsSnapshot.EMPTY);

    private long sampleIntervalNanos;

    private long lastSampleNanos = 0L;

    @Override
    public WorldStatsSnapshot snapshot()
    {
        return latest.get();
    }

    @Override
    public void start(int intervalSeconds)
    {
        sampleIntervalNanos = Math.max(1L, intervalSeconds) * 1_000_000_000L;
        lastSampleNanos = 0L;
    }

    @Override
    public void stop()
    {
        // Sampling is driven by the mod's tick handler; nothing to cancel here.
    }

    /**
     * Called from the NeoForge server-tick event. Rate-limited internally so a
     * tick handler running 20× per second doesn't oversample. Delegates the
     * platform-specific sampling to {@code platform}.
     */
    public void sample(NeoForgePlatform platform, Object server)
    {
        var now = System.nanoTime();
        if (now - lastSampleNanos < sampleIntervalNanos) {
            return;
        }
        lastSampleNanos = now;
        latest.set(platform.sampleWorldStats(server));
    }
}
