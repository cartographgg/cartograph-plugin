package gg.cartograph.plugin.neoforge.impl;

import gg.cartograph.plugin.common.events.WorldInfo;
import gg.cartograph.plugin.common.events.WorldMetrics;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;
import gg.cartograph.plugin.neoforge.NeoForgePlatform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NeoForge 26.1.x (Minecraft 26.1) implementation of {@link NeoForgePlatform},
 * compiled against the 26.1 {@code net.minecraft} API on JDK 25. Loaded via
 * ServiceLoader. Differs from the 1.21 impl only where the API diverged —
 * {@code ResourceKey.location()} became {@code identifier()} in 26.1.
 */
public class NeoForgePlatformImpl implements NeoForgePlatform
{

    private static MinecraftServer server(Object o) { return (MinecraftServer) o; }

    private static ServerPlayer player(Object o) { return (ServerPlayer) o; }

    @Override
    public double averageTickTimeMs(Object s) { return server(s).getAverageTickTimeNanos() / 1_000_000.0; }

    @Override
    public int playerCount(Object s) { return server(s).getPlayerCount(); }

    @Override
    public int maxPlayers(Object s) { return server(s).getMaxPlayers(); }

    @Override
    public int viewDistance(Object s) { return server(s).getPlayerList().getViewDistance(); }

    @Override
    public int simulationDistance(Object s) { return server(s).getPlayerList().getSimulationDistance(); }

    @Override
    public boolean usesAuthentication(Object s) { return server(s).usesAuthentication(); }

    @Override
    public String serverVersion(Object s) { return server(s).getServerVersion(); }

    @Override
    public List<WorldInfo> worlds(Object s)
    {
        var worlds = new ArrayList<WorldInfo>();
        for (var level : server(s).getAllLevels()) {
            var id = level.dimension().identifier();
            worlds.add(new WorldInfo(id.toString(), id.getPath()));
        }
        return worlds;
    }

    @Override
    public WorldStatsSnapshot sampleWorldStats(Object s)
    {
        var totalChunks = 0;
        var notable     = new ArrayList<WorldMetrics>();
        for (var level : server(s).getAllLevels()) {
            var chunks = level.getChunkSource().getLoadedChunksCount();
            totalChunks += chunks;
            if (WorldMetrics.isNotable(chunks, 0)) {
                notable.add(new WorldMetrics(level.dimension().identifier().toString(), chunks, null));
            }
        }
        return new WorldStatsSnapshot(totalChunks, null, notable);
    }

    @Override
    public boolean isServerPlayer(Object p) { return p instanceof ServerPlayer; }

    @Override
    public UUID playerUuid(Object p) { return player(p).getUUID(); }

    @Override
    public String playerName(Object p) { return player(p).getGameProfile().name(); }

    @Override
    public String playerLocale(Object p) { return player(p).clientInformation().language(); }

    @Override
    public String playerWorld(Object p) { return player(p).level().dimension().identifier().toString(); }
}
