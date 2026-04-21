package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.SessionTracker;
import gg.cartograph.plugin.common.events.LeaveReason;
import gg.cartograph.plugin.common.events.telemetry.PlayerLeaveTelemetryEvent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records leave telemetry on Bukkit servers.
 *
 * <p>Subscribes to kick and quit events. Kick events pre-mark the player's
 * disconnect reason (checking the server ban list to distinguish bans from
 * regular kicks). Quit events compute the session duration via the shared
 * {@link SessionTracker}, consume the pre-mark, and record a
 * {@link PlayerLeaveTelemetryEvent}.</p>
 *
 * <p>The kick handler uses {@link EventPriority#MONITOR} with
 * {@code ignoreCancelled = true} so it only fires for kicks that were not
 * cancelled by another plugin.</p>
 *
 * <p>If no kick was pre-marked, the quit handler falls back to Paper's
 * {@code PlayerQuitEvent.getReason()} via reflection to detect timeouts.
 * On Spigot servers without the Paper API the reason defaults to
 * {@link LeaveReason#QUIT}.</p>
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

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event)
    {
        var player = event.getPlayer();
        var uuid = player.getUniqueId();
        var isBanned = Bukkit.getBanList(BanList.Type.NAME).isBanned(player.getName());

        kickedPlayers.put(uuid, isBanned ? LeaveReason.BAN : LeaveReason.KICK);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        var player          = event.getPlayer();
        var uuid            = player.getUniqueId();
        var sessionDuration = sessionTracker.trackLeave(uuid);

        var reason = kickedPlayers.remove(uuid);
        if (reason == null) {
            reason = classifyReasonFromPaper(event);
        }

        cartograph.record(new PlayerLeaveTelemetryEvent(
                System.currentTimeMillis(),
                uuid,
                sessionDuration,
                reason,
                player.getWorld().getName()
        ));
    }

    /**
     * Attempts to extract Paper's {@code QuitReason} via reflection.
     * Falls back to {@link LeaveReason#QUIT} on Spigot or if reflection fails.
     */
    private LeaveReason classifyReasonFromPaper(PlayerQuitEvent event)
    {
        try {
            var getReasonMethod = event.getClass().getMethod("getReason");
            var quitReason      = getReasonMethod.invoke(event);
            var reasonName      = quitReason.toString();

            return switch (reasonName) {
                case "KICKED" -> LeaveReason.KICK;
                case "TIMED_OUT", "ERRONEOUS_STATE" -> LeaveReason.TIMEOUT;
                default -> LeaveReason.QUIT;
            };
        } catch (Exception ignored) {
            return LeaveReason.QUIT;
        }
    }
}
