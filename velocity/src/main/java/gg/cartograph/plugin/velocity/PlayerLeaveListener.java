package gg.cartograph.plugin.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.events.LeaveReason;
import gg.cartograph.plugin.common.events.telemetry.PlayerLeaveTelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records leave telemetry on Velocity proxies.
 *
 * <p>Subscribes to kicked-from-server and disconnect events.
 * {@link KickedFromServerEvent} fires when a backend server kicks the
 * player — the handler runs at {@link PostOrder#LAST} and only pre-marks
 * the UUID when the final result is
 * {@link KickedFromServerEvent.DisconnectPlayer} (i.e. the player is
 * actually being disconnected, not redirected to a fallback server).
 * Disconnect events consume the pre-mark, compute the session duration via
 * the shared session tracker, and record a
 * {@link PlayerLeaveTelemetryEvent}.</p>
 *
 * <p>The world is always {@code null} since the proxy does not track which
 * backend world the player was in.</p>
 */
class PlayerLeaveListener
{

    private final Cartograph cartograph;

    private final ConcurrentHashMap<UUID, LeaveReason> kickedPlayers = new ConcurrentHashMap<>();

    PlayerLeaveListener(Cartograph cartograph)
    {
        this.cartograph = cartograph;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onKickedFromServer(KickedFromServerEvent event)
    {
        if (event.getResult() instanceof KickedFromServerEvent.DisconnectPlayer) {
            kickedPlayers.put(event.getPlayer().getUniqueId(), LeaveReason.KICK);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event)
    {
        var logger          = cartograph.getLogger();
        var player          = event.getPlayer();
        var uuid            = player.getUniqueId();
        var sessionDuration = cartograph.getSessionTracker().trackLeave(uuid);

        var reason = kickedPlayers.remove(uuid);
        if (reason == null) {
            reason = LeaveReason.QUIT;
        }

        cartograph.record(new PlayerLeaveTelemetryEvent(
                System.currentTimeMillis(),
                uuid,
                sessionDuration,
                reason,
                null
        ));
        logger.debug("Player left: " + uuid + ", reason: " + reason + ", session: " + sessionDuration + "ms");
    }
}
