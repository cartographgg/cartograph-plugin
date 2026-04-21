package gg.cartograph.plugin.common.events.telemetry;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import gg.cartograph.plugin.common.events.EventTypes;
import gg.cartograph.plugin.common.events.WorldMetrics;

import java.util.List;

/**
 * Periodic metrics snapshot from a node. Sent roughly once per minute.
 *
 * <p>Every telemetry batch is implicitly a heartbeat for last-seen purposes
 * (the ingestion controller stamps {@code last_seen_at} on successful auth),
 * but the explicit heartbeat event carries the actual metrics used for
 * performance scoring and dashboard display.</p>
 *
 * <p>On proxy nodes, the server-specific fields ({@code tps}, {@code meanTickTime},
 * {@code peakTickTime}, {@code chunksLoaded}, {@code entitiesLoaded}) are
 * {@code null} and omitted from the payload. {@code playerCount} on a proxy
 * is the proxy-wide aggregate across all backends.</p>
 *
 * @param timestamp      the event timestamp (epoch ms)
 * @param tps            TPS samples over [1m, 5m, 15m] windows
 * @param meanTickTime   mean tick time (MSPT) in milliseconds since last heartbeat
 * @param peakTickTime   peak tick time (MSPT) in milliseconds since last heartbeat
 * @param playerCount    current player count
 * @param memoryUsed     heap bytes in use
 * @param memoryMax      heap bytes maximum
 * @param cpuProcessLoad JVM process CPU load, 0.0 to 1.0
 * @param cpuSystemLoad  system-wide CPU load, 0.0 to 1.0
 * @param threadCount    live thread count
 * @param chunksLoaded   total loaded chunks across all worlds
 * @param entitiesLoaded total loaded entities across all worlds
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HeartbeatTelemetryEvent(
        @JsonGetter("ts") Long timestamp,
        @JsonGetter("tp") double[] tps,
        @JsonGetter("mt") Double meanTickTime,
        @JsonGetter("pt") Double peakTickTime,
        @JsonGetter("pc") Integer playerCount,
        @JsonGetter("mu") Long memoryUsed,
        @JsonGetter("mm") Long memoryMax,
        @JsonGetter("cl") Double cpuProcessLoad,
        @JsonGetter("sl") Double cpuSystemLoad,
        @JsonGetter("th") Integer threadCount,
        @JsonGetter("cc") Integer chunksLoaded,
        @JsonGetter("ec") Integer entitiesLoaded,
        @JsonGetter("wm") List<WorldMetrics> worlds
) implements TelemetryEvent
{
    @Override
    @JsonGetter("t")
    public String type()
    {
        return EventTypes.HEARTBEAT;
    }
}