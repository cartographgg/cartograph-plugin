package gg.cartograph.plugin.common.events.spool;

/** A stored batch listed for replay. {@code handle} identifies it for load/delete. */
public record Spooled(long createdAtEpochMs, long sizeBytes, Object handle)
{
}
