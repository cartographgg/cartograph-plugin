package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.events.telemetry.PlayerJoinTelemetryEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.UUID;

/**
 * NeoForge listener that records a {@link PlayerJoinTelemetryEvent} when a player
 * logs in. All {@code net.minecraft} player access goes through {@link NeoForgePlatform}.
 */
class PlayerJoinListener
{

    private final Cartograph cartograph;

    private final NeoForgePlatform platform;

    PlayerJoinListener(Cartograph cartograph, NeoForgePlatform platform)
    {
        this.cartograph = cartograph;
        this.platform   = platform;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (cartograph.isProxyBackend()) {
            return;
        }

        Object player = event.getEntity();
        if (!platform.isServerPlayer(player)) {
            return;
        }

        var logger = cartograph.getLogger();
        var uuid   = platform.playerUuid(player);
        var name   = platform.playerName(player);

        Boolean isFloodgate = null;
        try {
            var floodgateClass    = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            var getInstance       = floodgateClass.getMethod("getInstance");
            var api               = getInstance.invoke(null);
            var isFloodgatePlayer = floodgateClass.getMethod("isFloodgatePlayer", UUID.class);
            if ((boolean) isFloodgatePlayer.invoke(api, uuid)) {
                isFloodgate = true;
            }
        } catch (Exception ignored) {
            logger.debug("Floodgate API not available");
        }

        cartograph.record(new PlayerJoinTelemetryEvent(
                System.currentTimeMillis(),
                uuid,
                name,
                null, // firstJoin (fj) — deferred; needs native player-data
                platform.playerProtocol(player),
                platform.playerLocale(player),
                platform.playerWorld(player),
                isFloodgate,
                platform.playerHostname(player)
        ));
        cartograph.getSessionTracker().trackJoin(uuid);
        logger.debug("Player joined: " + name + " (" + uuid + "), floodgate: " + isFloodgate);
    }
}
