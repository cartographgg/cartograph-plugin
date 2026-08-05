package gg.cartograph.plugin.common;

/** JVM GC activity over one heartbeat window: collections and total pause time (ms). */
public record GcDelta(long count, long timeMs)
{
}
