package gg.cartograph.plugin.common;

/**
 * Thread-safe accumulator for server tick durations and TPS calculation.
 *
 * <p>Server platforms feed tick durations via {@link #recordTick(double)}. The heartbeat
 * supplier reads {@link #getMeanTickTime()} and {@link #getPeakTickTime()}, then calls
 * {@link #reset()} to begin accumulating for the next interval.</p>
 *
 * <p>TPS is calculated using exponential moving averages over 1-minute, 5-minute, and
 * 15-minute windows — the same algorithm used internally by Minecraft servers. Call
 * {@link #getTps()} to retrieve the current averages.</p>
 */
public class TickSampler
{

    private static final double[] TPS_WINDOWS = {60.0, 300.0, 900.0};

    private final double[] tpsAverages = {20.0, 20.0, 20.0};

    private double sum;

    private double peak;

    private int count;

    private long lastTpsUpdate;

    private int ticksSinceLastUpdate;

    public TickSampler()
    {
        lastTpsUpdate = System.nanoTime();
    }

    public synchronized void recordTick(double milliseconds)
    {
        sum += milliseconds;
        count++;
        if (milliseconds > peak) {
            peak = milliseconds;
        }

        ticksSinceLastUpdate++;
        var now     = System.nanoTime();
        var elapsed = (now - lastTpsUpdate) / 1_000_000_000.0;

        // Update TPS averages every second
        if (elapsed >= 1.0) {
            var currentTps = ticksSinceLastUpdate / elapsed;
            for (int i = 0; i < TPS_WINDOWS.length; i++) {
                var exp = Math.exp(-elapsed / TPS_WINDOWS[i]);
                tpsAverages[i] = (tpsAverages[i] * exp) + (currentTps * (1.0 - exp));
            }
            ticksSinceLastUpdate = 0;
            lastTpsUpdate        = now;
        }
    }

    /**
     * Returns TPS averages over 1-minute, 5-minute, and 15-minute windows.
     */
    public synchronized double[] getTps()
    {
        return new double[] {tpsAverages[0], tpsAverages[1], tpsAverages[2]};
    }

    public synchronized double getMeanTickTime()
    {
        if (count == 0) {
            return 0.0;
        }
        return sum / count;
    }

    public synchronized double getPeakTickTime()
    {
        return peak;
    }

    public synchronized void reset()
    {
        sum   = 0.0;
        peak  = 0.0;
        count = 0;
    }
}
