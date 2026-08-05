package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;

import java.util.List;

/**
 * Wire envelope that wraps a batch of telemetry events for the Cartograph API.
 *
 * <p>Each flushed batch is wrapped in an envelope before being POSTed. The
 * envelope carries the schema version and the timestamp the batch was
 * assembled, allowing the ingestion server to handle format migrations.</p>
 *
 * <p>{@code sentAt} is stamped when the batch is assembled (in
 * {@code TelemetryPayloadFactory.prepare}), not when it is POSTed, and is
 * deliberately preserved across spool replays. For a batch that was spooled
 * and later retried, this value can therefore be significantly older than
 * the actual send time. A backend should treat it as an assembled-at
 * timestamp, not as a live send clock.</p>
 *
 * @param sentAt the epoch-millisecond timestamp when this batch was assembled
 * @param events the telemetry events in this batch
 */
public record TelemetryEnvelope(
        @JsonGetter("a") long sentAt,
        @JsonGetter("e") List<TelemetryEvent> events
)
{
    /**
     * Returns the schema version of this envelope format.
     *
     * @return always {@code 1}
     */
    @JsonGetter("v")
    public int schemaVersion()
    {
        return 1;
    }
}
