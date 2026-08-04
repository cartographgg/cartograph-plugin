package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.events.WorldInfo;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;

import java.util.List;
import java.util.UUID;

/**
 * Per-version boundary for every {@code net.minecraft} operation the NeoForge
 * module needs. The parent module holds server/player objects as {@code Object}
 * and calls this interface; each version module ({@code v1_21}, {@code v26_1})
 * supplies an impl compiled against its own {@code net.minecraft} generation and
 * registered via {@link java.util.ServiceLoader}.
 *
 * <p>This exists because {@code net.minecraft} genuinely diverges across
 * Minecraft lines — e.g. {@code ResourceKey.location()} (1.21) became
 * {@code identifier()} (26.1) — so the calls cannot live in a single
 * once-compiled parent.</p>
 *
 * <p>Every {@code Object} parameter is a {@code net.minecraft} handle the parent
 * never names: {@code server} is a {@code MinecraftServer}, {@code player} a
 * {@code ServerPlayer} (or {@code Player} to be narrowed via
 * {@link #isServerPlayer(Object)}).</p>
 */
public interface NeoForgePlatform
{
    double  averageTickTimeMs(Object server);

    int     playerCount(Object server);

    int     maxPlayers(Object server);

    int     viewDistance(Object server);

    int     simulationDistance(Object server);

    boolean usesAuthentication(Object server);

    String  serverVersion(Object server);

    List<WorldInfo> worlds(Object server);

    WorldStatsSnapshot sampleWorldStats(Object server);

    boolean isServerPlayer(Object player);

    UUID    playerUuid(Object player);

    String  playerName(Object player);

    String  playerLocale(Object player);

    String  playerWorld(Object player);
}
