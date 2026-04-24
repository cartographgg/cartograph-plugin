package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.events.LeaveReason;
import gg.cartograph.plugin.common.events.telemetry.PlayerLeaveTelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Records leave telemetry on NeoForge servers.
 *
 * <p>Subscribes to logout events, computes the session duration via the
 * shared session tracker, and records a
 * {@link PlayerLeaveTelemetryEvent}.</p>
 *
 * <p>Checks {@link Cartograph#isProxyBackend()} internally and returns
 * early when the server is running behind a proxy — matching the pattern
 * used by {@link PlayerJoinListener}.</p>
 *
 * <p>NeoForge does not expose a separate kick event, so the reason always
 * defaults to {@link LeaveReason#QUIT}.</p>
 */
class PlayerLeaveListener
{

    private final Cartograph cartograph;

    PlayerLeaveListener(Cartograph cartograph)
    {
        this.cartograph = cartograph;
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (cartograph.isProxyBackend()) {
            return;
        }

        var player = event.getEntity();

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var logger          = cartograph.getLogger();
        var uuid            = player.getUUID();
        var sessionDuration = cartograph.getSessionTracker().trackLeave(uuid);

        cartograph.record(new PlayerLeaveTelemetryEvent(
                System.currentTimeMillis(),
                uuid,
                sessionDuration,
                LeaveReason.QUIT,
                serverPlayer.level().dimension().location().toString()
        ));
        logger.debug("Player left: " + uuid + ", reason: QUIT, session: " + sessionDuration + "ms");
    }
}
