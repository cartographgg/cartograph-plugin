package gg.cartograph.plugin.common.config;

import java.time.Duration;

/** Bounds for the undelivered-batch store (on disk, or in RAM for memory mode). */
public class DiskBufferConfig
{
    private int maxSizeMb   = 8;
    private int maxAgeHours = 24;

    public int getMaxSizeMb() { return maxSizeMb; }
    public void setMaxSizeMb(int v) { this.maxSizeMb = Math.max(1, v); }

    public int getMaxAgeHours() { return maxAgeHours; }
    public void setMaxAgeHours(int v) { this.maxAgeHours = Math.max(1, v); }

    public long maxSizeBytes() { return (long) maxSizeMb * 1024L * 1024L; }
    public Duration maxAge() { return Duration.ofHours(maxAgeHours); }
}
