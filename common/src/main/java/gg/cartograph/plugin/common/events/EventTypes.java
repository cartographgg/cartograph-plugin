package gg.cartograph.plugin.common.events;

import java.util.Map;

/**
 * Constants mapping telemetry event types to their short wire codes.
 *
 * <p>The telemetry envelope uses single- or two-character codes for the {@code t}
 * field to minimise payload size. These constants provide named references so
 * event implementations and dispatchers can avoid magic strings.</p>
 *
 * <p>The reverse lookup via {@link #nameOf(String)} is intended for logging and
 * debugging only — the wire format is always the short code.</p>
 */
public final class EventTypes
{
    /** Boot — fired once on plugin enable. */
    public static final String BOOT = "b";

    /** Shutdown — fired on plugin disable. May be absent on crash. */
    public static final String SHUTDOWN = "x";

    /** Heartbeat — periodic metrics snapshot (TPS, players, memory, etc.). */
    public static final String HEARTBEAT = "h";

    /** Player join. */
    public static final String PLAYER_JOIN = "j";

    /** Player leave. */
    public static final String PLAYER_LEAVE = "l";

    /** Plugin/mod delta — runtime plugin add/remove between boots. */
    public static final String PLUGIN_DELTA = "pd";

    /**
     * Reverse lookup from short wire code to a human-readable name.
     * Intended for logging and debugging, not for serialisation.
     */
    private static final Map<String, String> NAMES = Map.of(
            BOOT, "boot",
            SHUTDOWN, "shutdown",
            HEARTBEAT, "heartbeat",
            PLAYER_JOIN, "player_join",
            PLAYER_LEAVE, "player_leave",
            PLUGIN_DELTA, "plugin_delta"
    );

    private EventTypes()
    {
    }

    /**
     * Returns a human-readable name for a short event code.
     *
     * @param code the short wire code (e.g. {@code "b"})
     *
     * @return the long name (e.g. {@code "boot"}), or {@code "unknown"} if unrecognised
     */
    public static String nameOf(String code)
    {
        return NAMES.getOrDefault(code, "unknown");
    }
}