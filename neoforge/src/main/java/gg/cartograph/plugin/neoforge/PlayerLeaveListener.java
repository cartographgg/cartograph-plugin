package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.events.LeaveReason;
import gg.cartograph.plugin.common.events.telemetry.PlayerLeaveTelemetryEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Records leave telemetry on NeoForge servers. All {@code net.minecraft} player
 * access goes through {@link NeoForgePlatform}.
 *
 * <p>NeoForge does not expose a separate kick event, so the reason always
 * defaults to {@link LeaveReason#QUIT}.</p>
 */
class PlayerLeaveListener
{

    private final Cartograph cartograph;

    private final NeoForgePlatform platform;

    PlayerLeaveListener(Cartograph cartograph, NeoForgePlatform platform)
    {
        this.cartograph = cartograph;
        this.platform   = platform;
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (cartograph.isProxyBackend()) {
            return;
        }

        Object player = event.getEntity();
        if (!platform.isServerPlayer(player)) {
            return;
        }

        var logger          = cartograph.getLogger();
        var uuid            = platform.playerUuid(player);
        var sessionDuration = cartograph.getSessionTracker().trackLeave(uuid);

        cartograph.record(new PlayerLeaveTelemetryEvent(
                System.currentTimeMillis(),
                uuid,
                sessionDuration,
                LeaveReason.QUIT,
                platform.playerWorld(player)
        ));
        logger.debug("Player left: " + uuid + ", reason: QUIT, session: " + sessionDuration + "ms");
    }
}
