package gg.cartograph.plugin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.SessionTracker;
import gg.cartograph.plugin.common.events.telemetry.PlayerJoinTelemetryEvent;

import java.util.UUID;

/**
 * Velocity listener that records a {@link PlayerJoinTelemetryEvent} when a player
 * completes login to the proxy.
 */
class PlayerJoinListener
{

    private final Cartograph    cartograph;
    private final SessionTracker sessionTracker;

    PlayerJoinListener(Cartograph cartograph, SessionTracker sessionTracker)
    {
        this.cartograph     = cartograph;
        this.sessionTracker = sessionTracker;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event)
    {
        var player = event.getPlayer();
        var ip     = player.getRemoteAddress().getAddress().getHostAddress();
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
                player.getUsername(),
                null,
                player.getProtocolVersion().getProtocol(),
                player.getEffectiveLocale() != null ? player.getEffectiveLocale().toString() : null,
                null,
                isFloodgate,
                ipHash
        ));
        sessionTracker.trackJoin(player.getUniqueId());
    }
}
