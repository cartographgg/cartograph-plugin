package gg.cartograph.plugin.common.events.telemetry;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import gg.cartograph.plugin.common.events.EventTypes;
import gg.cartograph.plugin.common.events.ShutdownReason;

/**
 * Telemetry event fired once when the Cartograph plugin disables, typically
 * on clean server shutdown or restart.
 *
 * <p>This event is best-effort: JVM hard kills, OOM, and power loss all skip
 * plugin disable hooks, so the absence of a shutdown event does not itself
 * indicate anything went wrong. Uptime scoring on the ingestion side relies
 * on heartbeat gaps rather than the presence or absence of this event.</p>
 *
 * @param timestamp the event timestamp (epoch ms)
 * @param uptime    the node's uptime at shutdown, in milliseconds
 * @param reason    an optional, best-effort reason for the shutdown — see
 *                  {@link ShutdownReason}. May be {@code null} if the plugin
 *                  cannot determine it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShutdownTelemetryEvent(
        @JsonGetter("ts") Long timestamp,
        @JsonGetter("up") Long uptime,
        @JsonGetter("r") ShutdownReason reason
) implements TelemetryEvent
{
    @Override
    @JsonGetter("t")
    public String type()
    {
        return EventTypes.SHUTDOWN;
    }
}