package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.GcDelta;
import gg.cartograph.plugin.common.TickPercentiles;
import gg.cartograph.plugin.common.events.WorldInfo;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeoForgeHeartbeatTest
{

    /** Fake platform returning fixed values — no server, no net.minecraft. */
    static final class FakePlatform implements NeoForgePlatform
    {
        public double averageTickTimeMs(Object s)  { return 12.5; }
        public int playerCount(Object s)           { return 7; }
        public int maxPlayers(Object s)            { return 20; }
        public int viewDistance(Object s)          { return 10; }
        public int simulationDistance(Object s)    { return 8; }
        public boolean usesAuthentication(Object s) { return true; }
        public String serverVersion(Object s)      { return "26.1"; }
        public List<WorldInfo> worlds(Object s)    { return List.of(new WorldInfo("minecraft:overworld", "overworld")); }
        public WorldStatsSnapshot sampleWorldStats(Object s) { return new WorldStatsSnapshot(42, null, List.of()); }
        public boolean isServerPlayer(Object p)    { return true; }
        public UUID playerUuid(Object p)           { return new UUID(0, 1); }
        public String playerName(Object p)         { return "Steve"; }
        public String playerLocale(Object p)       { return "en_us"; }
        public String playerWorld(Object p)        { return "minecraft:overworld"; }
    }

    @Test
    void heartbeatAssemblesFromPlatformAndPercentiles()
    {
        var event = NeoForgeHeartbeat.build(
                new FakePlatform(), "server", 60,
                new TickPercentiles(1.0, 2.0, 3.0, 4.0),
                new WorldStatsSnapshot(42, null, List.of()),
                new GcDelta(2L, 15L));

        assertEquals(7, event.playerCount());
        assertEquals(60, event.effectiveInterval());
        assertEquals(4.0, event.peakTickTime());
        assertEquals(1.0, event.p50());
        assertEquals(2.0, event.p95());
        assertEquals(3.0, event.p99());
        assertEquals(42, event.chunksLoaded());
        assertEquals(12.5, event.tps()[0]);
        assertEquals(2L, event.gcCount());
        assertEquals(15L, event.gcTimeMs());
    }
}
