package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Classification of why a player left a node.
 *
 * <p>The plugin maps Bukkit's {@code PlayerQuitEvent}, {@code PlayerKickEvent},
 * and (where detectable) ban enforcement into one of these values. On proxies,
 * Velocity/BungeeCord disconnect events are similarly classified.</p>
 */
public enum LeaveReason
{
    /** Player disconnected voluntarily. */
    QUIT("quit"),

    /** Player was kicked (operator action, plugin, or proxy). */
    KICK("kick"),

    /** Connection dropped without a clean disconnect — network or client crash. */
    TIMEOUT("timeout"),

    /** Player was disconnected as part of a ban being applied. */
    BAN("ban");

    private final String value;

    LeaveReason(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String value()
    {
        return value;
    }
}