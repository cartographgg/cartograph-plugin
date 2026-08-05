package gg.cartograph.plugin.common;

import java.lang.management.ManagementFactory;
import java.util.function.Supplier;

/**
 * Accumulates JVM garbage-collection activity per heartbeat window.
 *
 * <p>{@link java.lang.management.GarbageCollectorMXBean} reports totals since JVM
 * start; the heartbeat needs GC in the current window, so {@link #sample()} returns
 * the delta since the previous call and advances its baseline — mirroring how
 * {@link TickSampler} is read-and-reset each heartbeat. The baseline is stamped at
 * construction (eagerly, at plugin start), so the first heartbeat's delta covers
 * boot to first heartbeat.</p>
 */
public class GcSampler
{
    private final Supplier<long[]> reader; // {cumulativeCount, cumulativeTimeMs}

    private long lastCount;
    private long lastTimeMs;

    public GcSampler()
    {
        this(GcSampler::readMxBeans);
    }

    GcSampler(Supplier<long[]> reader)
    {
        this.reader     = reader;
        long[] base     = reader.get();
        this.lastCount  = base[0];
        this.lastTimeMs = base[1];
    }

    public synchronized GcDelta sample()
    {
        long[] current  = reader.get();
        long deltaCount = Math.max(0, current[0] - lastCount);
        long deltaTime  = Math.max(0, current[1] - lastTimeMs);
        lastCount  = current[0];
        lastTimeMs = current[1];
        return new GcDelta(deltaCount, deltaTime);
    }

    private static long[] readMxBeans()
    {
        long count = 0;
        long time  = 0;
        for (var bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            long c = bean.getCollectionCount(); // -1 if unavailable
            long t = bean.getCollectionTime();
            if (c > 0) {
                count += c;
            }
            if (t > 0) {
                time += t;
            }
        }
        return new long[] {count, time};
    }
}
