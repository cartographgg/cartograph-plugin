package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.util.Hostnames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Captures the hostname the player used to connect ({@link PlayerLoginEvent#getHostname()})
 * and stashes it for {@link PlayerJoinListener} to attach to the join telemetry event.
 *
 * <p>{@code PlayerLoginEvent} fires before {@code PlayerJoinEvent}. This handler runs at
 * {@link EventPriority#MONITOR} so {@code getResult()} reflects the final decision after
 * every other plugin has voted; only genuinely allowed logins with a usable hostname are
 * stored. A stashed entry is cleared when the player joins. If a client disconnects between
 * login and join it leaves a single entry, which is overwritten on that player's next login
 * rather than accumulating.</p>
 */
class PlayerLoginListener implements Listener
{
    private final PlayerHostnameStash stash;

    PlayerLoginListener(PlayerHostnameStash stash)
    {
        this.stash = stash;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }

        var hostname = Hostnames.normalize(event.getHostname());
        if (hostname != null) {
            stash.put(event.getPlayer().getUniqueId(), hostname);
        }
    }
}
