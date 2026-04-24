package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.events.telemetry.PlayerJoinTelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/**
 * Bukkit listener that records a {@link PlayerJoinTelemetryEvent} when a player joins.
 */
class PlayerJoinListener implements Listener
{

    private final Cartograph cartograph;

    PlayerJoinListener(Cartograph cartograph)
    {
        this.cartograph = cartograph;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        var logger = cartograph.getLogger();
        var player = event.getPlayer();
        var ip     = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : null;
        var ipHash = cartograph.getIpHasher() != null ? cartograph.getIpHasher().hash(ip) : null;

        Boolean isFloodgate = null;
        try {
            var floodgateClass    = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            var getInstance       = floodgateClass.getMethod("getInstance");
            var api               = getInstance.invoke(null);
            var isFloodgatePlayer = floodgateClass.getMethod("isFloodgatePlayer", UUID.class);
            if ((boolean) isFloodgatePlayer.invoke(api, player.getUniqueId())) {
                isFloodgate = true;
            }
        } catch (Exception ignored) {
            logger.debug("Floodgate API not available");
        }

        cartograph.record(new PlayerJoinTelemetryEvent(
                System.currentTimeMillis(),
                player.getUniqueId(),
                player.getName(),
                !player.hasPlayedBefore(),
                null,
                player.getLocale(),
                player.getWorld().getName(),
                isFloodgate,
                ipHash
        ));
        cartograph.getSessionTracker().trackJoin(player.getUniqueId());
        logger.debug("Player joined: " + player.getName() + " (" + player.getUniqueId() + "), floodgate: " + isFloodgate);
    }
}
