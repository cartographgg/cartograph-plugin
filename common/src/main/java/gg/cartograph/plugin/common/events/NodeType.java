package gg.cartograph.plugin.common;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Role of a Minecraft process in a server's topology.
 *
 * <ul>
 *   <li>{@link #STANDALONE} — single-node server, not behind a proxy.</li>
 *   <li>{@link #PROXY} — Velocity or BungeeCord proxy fronting one or more backends.</li>
 *   <li>{@link #BACKEND} — Bukkit-family server sitting behind a proxy.</li>
 * </ul>
 */
public enum NodeType
{
    STANDALONE("standalone"),
    PROXY("proxy"),
    BACKEND("backend");

    private final String value;

    NodeType(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String value()
    {
        return value;
    }
}