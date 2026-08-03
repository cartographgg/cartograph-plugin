package gg.cartograph.plugin.common;

/** Tick-duration percentiles (ms) over a heartbeat window. {@code max} is p100. */
public record TickPercentiles(double p50, double p95, double p99, double max)
{
}
