package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Best-effort classification of why a node shut down.
 *
 * <p>In practice, the plugin usually cannot distinguish these with certainty —
 * {@link #CLEAN} is what you'll see most of the time. The enum exists so that
 * if a future platform adapter can detect a restart signal or a reload call,
 * it has a place to report it without a schema change.</p>
 */
public enum ShutdownReason
{
    /** Normal server stop — {@code /stop} or process SIGTERM handled cleanly. */
    CLEAN("clean"),

    /** Restart detected — e.g. a restart script or a known restart plugin. */
    RESTART("restart"),

    /** Plugin disabled via reload or manual disable, server still running. */
    RELOAD("reload");

    private final String value;

    ShutdownReason(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String value()
    {
        return value;
    }
}