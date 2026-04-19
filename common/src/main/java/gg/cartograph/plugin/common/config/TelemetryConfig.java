package gg.cartograph.plugin.common.config;

/**
 * Configuration for a single telemetry type (e.g. heartbeat, TPS sampling, latency).
 *
 * <p>Each telemetry type can be independently enabled or disabled, and its collection
 * interval can be adjusted. Setting {@code enabled} to {@code false} prevents the
 * plugin from collecting or sending that telemetry type entirely.</p>
 *
 * <p>Defaults: enabled = {@code true}, interval = {@code 60} seconds.</p>
 */
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
