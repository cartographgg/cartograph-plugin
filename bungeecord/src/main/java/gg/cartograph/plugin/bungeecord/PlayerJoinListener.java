package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.SessionTracker;
import gg.cartograph.plugin.common.events.telemetry.PlayerJoinTelemetryEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * BungeeCord listener that records a {@link PlayerJoinTelemetryEvent} when a player
 * completes login to the proxy.
 */
class PlayerJoinListener implements Listener
{

    private final Cartograph    cartograph;
    private final SessionTracker sessionTracker;

    PlayerJoinListener(Cartograph cartograph, SessionTracker sessionTracker)
    {
        this.cartograph     = cartograph;
        this.sessionTracker = sessionTracker;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event)
    {
        var player = event.getPlayer();
        var ip = player.getSocketAddress() instanceof InetSocketAddress addr
                 ? addr.getAddress().getHostAddress() : null;
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
        }

        cartograph.record(new PlayerJoinTelemetryEvent(
                System.currentTimeMillis(),
                player.getUniqueId(),
                player.getName(),
                null,
                null,
                player.getLocale() != null ? player.getLocale().toString() : null,
                null,
                isFloodgate,
                ipHash
        ));
        sessionTracker.trackJoin(player.getUniqueId());
    }
}
