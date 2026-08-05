package gg.cartograph.plugin.common.events.spool;

/** A serialized, ready-to-send telemetry payload and the wall-clock ms it was prepared. */
public record PreparedBatch(byte[] payload, long createdAtEpochMs)
{
}
