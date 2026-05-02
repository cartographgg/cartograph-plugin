package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.events.WorldMetrics;
import gg.cartograph.plugin.common.world.WorldStatsProvider;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NeoForge world stats provider.
 *
 * <p>Sampling happens on the server tick thread via
 * {@link CartographNeoForgeMod#onServerTick}, which already runs on the main
 * server thread. The mod calls {@link #sample(MinecraftServer)} once per
 * configured interval (rate-limited internally) and the heartbeat reads the
 * latest snapshot via {@link #snapshot()} from any thread.</p>
 *
 * <p>Entity counts are not sampled because Vanilla's per-level entity tracking
 * does not expose a cheap aggregate; reporting just chunks keeps the path
 * thread-safe and matches the existing NeoForge heartbeat shape (the original
 * code already passed {@code entitiesLoaded == null}).</p>
 */
public class NeoForgeWorldStatsProvider implements WorldStatsProvider
{

    private final AtomicReference<WorldStatsSnapshot> latest = new AtomicReference<>(WorldStatsSnapshot.EMPTY);

    private long sampleIntervalNanos;

    private long lastSampleNanos = 0L;

    @Override
    public WorldStatsSnapshot snapshot()
    {
        return latest.get();
    }

    @Override
    public void start(int intervalSeconds)
    {
        sampleIntervalNanos = Math.max(1L, intervalSeconds) * 1_000_000_000L;
        lastSampleNanos = 0L;
    }

    @Override
    public void stop()
    {
        // Sampling is driven by the mod's tick handler; nothing to cancel here.
    }

    /**
     * Called from the NeoForge server-tick event. Rate-limited internally so a
     * tick handler running 20× per second doesn't oversample.
     */
    public void sample(MinecraftServer server)
    {
        var now = System.nanoTime();
        if (now - lastSampleNanos < sampleIntervalNanos) {
            return;
        }
        lastSampleNanos = now;

        var totalChunks = 0;
        var notable     = new ArrayList<WorldMetrics>();

        for (var level : server.getAllLevels()) {
            var chunks = level.getChunkSource().getLoadedChunksCount();
            totalChunks += chunks;
            if (WorldMetrics.isNotable(chunks, 0)) {
                notable.add(new WorldMetrics(
                        level.dimension().location().toString(),
                        chunks,
                        null
                ));
            }
        }

        latest.set(new WorldStatsSnapshot(totalChunks, null, notable));
    }
}
