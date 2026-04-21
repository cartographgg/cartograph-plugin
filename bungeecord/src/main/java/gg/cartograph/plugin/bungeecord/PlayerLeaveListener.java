package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.SessionTracker;
import gg.cartograph.plugin.common.events.LeaveReason;
import gg.cartograph.plugin.common.events.telemetry.PlayerLeaveTelemetryEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records leave telemetry on BungeeCord proxies.
 *
 * <p>Subscribes to server-kick and disconnect events. {@link ServerKickEvent}
 * fires when a backend server kicks the player — the handler pre-marks the
 * UUID as {@link LeaveReason#KICK} if the event has not been cancelled (i.e.
 * the player was not redirected to a fallback server). Disconnect events
 * consume the pre-mark, compute the session duration via the shared
 * {@link SessionTracker}, and record a {@link PlayerLeaveTelemetryEvent}.</p>
 *
 * <p>The kick handler runs at {@link EventPriority#HIGHEST} and checks
 * {@code isCancelled()} so it only marks kicks that were not intercepted
 * by a fallback plugin. The world is always {@code null} since the proxy
 * does not track which backend world the player was in.</p>
 */
class PlayerLeaveListener implements Listener
{

    private final Cartograph    cartograph;
    private final SessionTracker sessionTracker;

    private final ConcurrentHashMap<UUID, LeaveReason> kickedPlayers = new ConcurrentHashMap<>();

    PlayerLeaveListener(Cartograph cartograph, SessionTracker sessionTracker)
    {
        this.cartograph     = cartograph;
        this.sessionTracker = sessionTracker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event)
    {
        if (!event.isCancelled()) {
            kickedPlayers.put(event.getPlayer().getUniqueId(), LeaveReason.KICK);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event)
    {
        var player          = event.getPlayer();
        var uuid            = player.getUniqueId();
        var sessionDuration = sessionTracker.trackLeave(uuid);

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
    }
}
