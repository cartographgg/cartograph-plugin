package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Backend server entry reported by a proxy in its boot event.
 *
 * <p>The {@code name} is the identifier the proxy uses internally (e.g.
 * Velocity's configured server name, BungeeCord's server entry key).
 * The {@code address} is the host:port the proxy connects to.</p>
 */
public record BackendInfo(
        @JsonGetter("n") String name,
        @JsonGetter("a") String address
)
{
}