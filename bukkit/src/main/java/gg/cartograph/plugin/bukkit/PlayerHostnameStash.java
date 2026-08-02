package gg.cartograph.plugin.bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridges the connection hostname from {@code PlayerLoginEvent} (which fires
 * first and exposes {@code getHostname()}) to {@code PlayerJoinEvent} (where the
 * telemetry event is built).
 *
 * <p>Backed by a {@link ConcurrentHashMap} because on Folia the login and join
 * for a player may run on different region threads. {@link #take(UUID)} removes
 * on read, so a successful join never leaks an entry.</p>
 */
final class PlayerHostnameStash
{
    private final Map<UUID, String> byPlayer = new ConcurrentHashMap<>();

    void put(UUID uuid, String hostname)
    {
        byPlayer.put(uuid, hostname);
    }

    String take(UUID uuid)
    {
        return byPlayer.remove(uuid);
    }
}
