package gg.cartograph.plugin.common.config;

public class TelemetryConfig
{

    private boolean enabled = true;

    private int interval = 60;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public int getInterval()
    {
        return interval;
    }

    public void setInterval(int interval)
    {
        this.interval = interval;
    }
}
