package gg.cartograph.plugin.common.world;

import gg.cartograph.plugin.common.events.WorldMetrics;

import java.util.List;

/**
 * Thread-safe snapshot of world telemetry stats, produced by a
 * {@link WorldStatsProvider} and consumed by a heartbeat event builder.
 *
 * <p>Either count may be {@code null} if the platform cannot safely or
 * cheaply sample that dimension — Folia, for example, cannot iterate
 * entities globally without per-region scheduling, so its provider
 * reports {@code entitiesLoaded == null}. The heartbeat event format
 * already excludes {@code null} fields from the wire payload, so a
 * {@code null} count is omitted rather than reported as zero.</p>
 *
 * @param chunksLoaded   total loaded chunks across all worlds, or {@code null} if unsampled
 * @param entitiesLoaded total loaded entities across all worlds, or {@code null} if unsampled
 * @param notableWorlds  worlds whose chunk or entity counts cross the
 *                       notability thresholds in {@link WorldMetrics}; never {@code null}
 */
public record WorldStatsSnapshot(
        Integer chunksLoaded,
        Integer entitiesLoaded,
        List<WorldMetrics> notableWorlds
)
{
    public static final WorldStatsSnapshot EMPTY = new WorldStatsSnapshot(null, null, List.of());
}
