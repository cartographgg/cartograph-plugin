package gg.cartograph.plugin.common.util;

import java.util.Locale;

/**
 * Normalises the raw Minecraft handshake server address (the "virtual host")
 * into a clean, lowercase domain suitable for telemetry.
 *
 * <p>The raw value can carry a Forge {@code FML} marker, legacy
 * BungeeCord/Velocity IP-forwarding data (NUL-delimited: host, then client IP, UUID, and properties),
 * an SRV trailing dot, a port, and inconsistent case. Cutting at the first NUL
 * also guarantees a real client IP embedded in forwarding data never survives
 * into the emitted value.</p>
 */
public final class Hostnames
{
    private Hostnames()
    {
    }

    /**
     * @param raw the raw handshake host (may be {@code null})
     *
     * @return the cleaned lowercase host, or {@code null} if nothing usable remains
     */
    public static String normalize(String raw)
    {
        if (raw == null) {
            return null;
        }

        var value = raw;

        // 1. Cut at the first NUL - removes FML marker and forwarding data (PII safeguard).
        var nul = value.indexOf('\u0000');
        if (nul >= 0) {
            value = value.substring(0, nul);
        }

        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }

        // 2. Strip the port.
        if (value.startsWith("[")) {
            // Bracketed IPv6, e.g. [::1]:25565 or [::1]
            var close = value.indexOf(']');
            if (close > 0) {
                value = value.substring(1, close);
            }
        } else if (value.indexOf(':') >= 0 && value.indexOf(':') == value.lastIndexOf(':')) {
            // Exactly one colon -> host:port
            value = value.substring(0, value.indexOf(':'));
        }
        // More than one colon and no brackets -> bare IPv6 literal, left as-is.

        // 3. Strip trailing dots (SRV artifact).
        while (value.endsWith(".")) {
            value = value.substring(0, value.length() - 1);
        }

        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }

        return value.toLowerCase(Locale.ROOT);
    }
}
