package gg.cartograph.plugin.common.config;

import java.util.Locale;

/** How the buffer treats a batch that fails to send. */
public enum FailureMode
{
    DISK, MEMORY, NONE;

    /** Case-insensitive parse; unknown or null falls back to {@link #DISK}. */
    public static FailureMode from(String value)
    {
        if (value == null) {
            return DISK;
        }
        try {
            return FailureMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return DISK;
        }
    }
}
