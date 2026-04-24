package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.events.telemetry.PlayerJoinTelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * NeoForge listener that records a {@link PlayerJoinTelemetryEvent} when a player
 * logs in to the server.
 */
class PlayerJoinListener
{

    private final Cartograph cartograph;

    PlayerJoinListener(Cartograph cartograph)
    {
        this.cartograph = cartograph;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (cartograph.isProxyBackend()) {
            return;
        }

        var player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var    logger     = cartograph.getLogger();
        var    connection = serverPlayer.connection.getConnection();
        var    address    = connection.getRemoteAddress();
        String ip         = null;
        if (address instanceof InetSocketAddress inetAddress) {
            ip = inetAddress.getAddress().getHostAddress();
        }
        var ipHash = cartograph.getIpHasher() != null ? cartograph.getIpHasher().hash(ip) : null;

        Boolean isFloodgate = null;
        try {
            var floodgateClass    = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            var getInstance       = floodgateClass.getMethod("getInstance");
            var api               = getInstance.invoke(null);
            var isFloodgatePlayer = floodgateClass.getMethod("isFloodgatePlayer", UUID.class);
            if ((boolean) isFloodgatePlayer.invoke(api, player.getUUID())) {
                isFloodgate = true;
            }
        } catch (Exception ignored) {
            logger.debug("Floodgate API not available");
        }

        cartograph.record(new PlayerJoinTelemetryEvent(
                System.currentTimeMillis(),
                player.getUUID(),
                player.getGameProfile().getName(),
                null,
                null,
                serverPlayer.clientInformation().language(),
                serverPlayer.level().dimension().location().toString(),
                isFloodgate,
                ipHash
        ));
        cartograph.getSessionTracker().trackJoin(player.getUUID());
        logger.debug("Player joined: " + player.getGameProfile().getName() + " (" + player.getUUID() + "), floodgate: " + isFloodgate);
    }
}
