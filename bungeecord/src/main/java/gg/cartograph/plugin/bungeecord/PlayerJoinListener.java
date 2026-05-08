package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.events.telemetry.PlayerJoinTelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

/**
 * BungeeCord listener that records a {@link PlayerJoinTelemetryEvent} when a player
 * completes login to the proxy.
 */
class PlayerJoinListener implements Listener
{

    private final Cartograph cartograph;

    PlayerJoinListener(Cartograph cartograph)
    {
        this.cartograph = cartograph;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event)
    {
        var logger = cartograph.getLogger();
        var player = event.getPlayer();

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
                null,
                null,
                player.getLocale() != null ? player.getLocale().toString() : null,
                null,
                isFloodgate
        ));
        cartograph.getSessionTracker().trackJoin(player.getUniqueId());
        logger.debug("Player joined: " + player.getName() + " (" + player.getUniqueId() + "), floodgate: " + isFloodgate);
    }
}
