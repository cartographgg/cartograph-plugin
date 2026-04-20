package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Marker interface for telemetry event DTOs.
 *
 * <p>Each telemetry event (heartbeat, TPS sample, latency, etc.) implements this
 * interface and provides a type discriminator used for JSON serialization.</p>
 */
public interface TelemetryEvent
{
    /**
     * Returns the event type discriminator (e.g. "heartbeat", "tps_sample", "latency").
     *
     * @return the event type string, used as the JSON type field
     */
    @JsonGetter("t")
    String type();

    /**
     * Returns the timestamp of the event.
     *
     * @return the event timestamp, in milliseconds since the Unix epoch
     */
    @JsonGetter("ts")
    Long timestamp();
}
