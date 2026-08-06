package gg.cartograph.plugin.neoforge.impl;

import gg.cartograph.plugin.common.events.WorldInfo;
import gg.cartograph.plugin.common.events.WorldMetrics;
import gg.cartograph.plugin.common.util.Hostnames;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;
import gg.cartograph.plugin.neoforge.CartographHandshakeInfo;
import gg.cartograph.plugin.neoforge.NeoForgePlatform;
import gg.cartograph.plugin.neoforge.mixin.ServerCommonListenerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NeoForge 21.x (Minecraft 1.21) implementation of {@link NeoForgePlatform},
 * compiled against the 1.21 {@code net.minecraft} API. Loaded via ServiceLoader.
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
            var id = level.dimension().location();
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
                notable.add(new WorldMetrics(level.dimension().location().toString(), chunks, null));
            }
        }
        return new WorldStatsSnapshot(totalChunks, null, notable);
    }

    @Override
    public boolean isServerPlayer(Object p) { return p instanceof ServerPlayer; }

    @Override
    public UUID playerUuid(Object p) { return player(p).getUUID(); }

    @Override
    public String playerName(Object p) { return player(p).getGameProfile().getName(); }

    @Override
    public String playerLocale(Object p) { return player(p).clientInformation().language(); }

    @Override
    public String playerWorld(Object p) { return player(p).level().dimension().location().toString(); }

    @Override
    public Integer playerProtocol(Object p)
    {
        var info = handshakeInfo(player(p));
        return info == null ? null : info.cartographProtocol();
    }

    @Override
    public String playerHostname(Object p)
    {
        var info = handshakeInfo(player(p));
        return info == null ? null : Hostnames.normalize(info.cartographHostname());
    }

    private static CartographHandshakeInfo handshakeInfo(ServerPlayer player)
    {
        var listener = player.connection;
        if (listener == null) {
            return null;
        }
        var connection = ((ServerCommonListenerAccessor) listener).cartographConnection();
        return connection instanceof CartographHandshakeInfo info ? info : null;
    }
}
