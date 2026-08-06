package gg.cartograph.plugin.common.config;

/** Caps for send backoff. */
public class BackoffConfig
{
    private int maxSeconds           = 900;  // exponential ceiling (15 min)
    private int retryAfterCapSeconds = 3600; // honor server Retry-After up to 1h

    public int getMaxSeconds()
    {
        return maxSeconds;
    }

    public void setMaxSeconds(int v)
    {
        this.maxSeconds = Math.max(1, v);
    }

    public int getRetryAfterCapSeconds()
    {
        return retryAfterCapSeconds;
    }

    public void setRetryAfterCapSeconds(int v)
    {
        this.retryAfterCapSeconds = Math.max(1, v);
    }
}
