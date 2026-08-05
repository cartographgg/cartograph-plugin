package gg.cartograph.plugin.common.config;

/**
 * Controls how telemetry events are batched before being sent to the Cartograph API.
 *
 * <p>Rather than sending each event individually, events are accumulated in a buffer
 * and flushed as a batch when either threshold is reached — whichever comes first.</p>
 *
 * <p>Defaults:</p>
 * <ul>
 *     <li><b>sizeThreshold</b> — flush after 50 events</li>
 *     <li><b>timeThreshold</b> — flush after 60 seconds</li>
 * </ul>
 */
public class BufferConfig
{

    private int sizeThreshold = 50;

    private int timeThreshold = 60;

    private FailureMode failureMode = FailureMode.DISK;
    private DiskBufferConfig disk   = new DiskBufferConfig();
    private BackoffConfig backoff   = new BackoffConfig();

    public int getSizeThreshold()
    {
        return sizeThreshold;
    }

    public void setSizeThreshold(int sizeThreshold)
    {
        this.sizeThreshold = sizeThreshold;
    }

    public int getTimeThreshold()
    {
        return timeThreshold;
    }

    public void setTimeThreshold(int timeThreshold)
    {
        this.timeThreshold = timeThreshold;
    }

    public FailureMode getFailureMode() { return failureMode; }
    public void setFailureMode(FailureMode failureMode) { this.failureMode = failureMode; }

    public DiskBufferConfig getDisk() { return disk; }
    public void setDisk(DiskBufferConfig disk) { this.disk = disk; }

    public BackoffConfig getBackoff() { return backoff; }
    public void setBackoff(BackoffConfig backoff) { this.backoff = backoff; }
}
