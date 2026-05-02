package gg.cartograph.plugin.common.world;

/**
 * Platform-specific source of world telemetry stats safe to read from any thread.
 *
 * <p>The Cartograph heartbeat scheduler runs on a generic
 * {@code ScheduledExecutorService} thread, which is NOT the platform's main
 * tick thread. Direct calls to platform world APIs (e.g. Bukkit's
 * {@code World#getEntities()}) from that thread violate platform thread-affinity
 * guarantees and trigger Paper's {@code AsyncCatcher} or risk
 * {@code ConcurrentModificationException} on Spigot. Implementations of this
 * interface schedule their own sampling on the appropriate thread (a Bukkit
 * scheduler task on Paper/Spigot, the global region scheduler on Folia, the
 * {@code ServerTickEvent} on NeoForge) and cache the latest result for the
 * heartbeat thread to read via {@link #snapshot()}.</p>
 *
 * <p>Use {@link #empty()} on platforms with no worlds to inspect (proxies).</p>
 */
public interface WorldStatsProvider
{
    /**
     * Returns the most recently sampled snapshot. Safe to call from any thread.
     */
    WorldStatsSnapshot snapshot();

    /**
     * Begins periodic sampling. Implementations should perform an immediate
     * first sample so the next heartbeat does not see {@link WorldStatsSnapshot#EMPTY}.
     *
     * @param intervalSeconds the sampling cadence, in seconds — typically the heartbeat interval
     */
    default void start(int intervalSeconds) {}

    /**
     * Stops periodic sampling and releases any platform handles. Idempotent.
     */
    default void stop() {}

    /**
     * Returns a no-op provider that always reports {@link WorldStatsSnapshot#EMPTY}.
     * Use on proxy platforms (Velocity, BungeeCord) which have no worlds.
     */
    static WorldStatsProvider empty()
    {
        return () -> WorldStatsSnapshot.EMPTY;
    }
}
