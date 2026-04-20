package gg.cartograph.plugin.common;

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
    String type();
}
