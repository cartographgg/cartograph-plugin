package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.events.WorldMetrics;
import gg.cartograph.plugin.common.world.WorldStatsProvider;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Paper / Spigot world stats provider.
 *
 * <p>Samples world chunk and entity counts via {@link org.bukkit.scheduler.BukkitScheduler#runTaskTimer},
 * which runs on the server's main tick thread — the only thread on which
 * {@code World#getEntities()} and {@code World#getLoadedChunks()} are safe to call.
 * The sampled snapshot is published via an {@link AtomicReference} for the
 * heartbeat thread to read.</p>
 */
public class BukkitWorldStatsProvider implements WorldStatsProvider
{

    private final JavaPlugin plugin;

    private final AtomicReference<WorldStatsSnapshot> latest = new AtomicReference<>(WorldStatsSnapshot.EMPTY);

    private BukkitTask sampleTask;

    public BukkitWorldStatsProvider(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public WorldStatsSnapshot snapshot()
    {
        return latest.get();
    }

    @Override
    public void start(int intervalSeconds)
    {
        var ticks = Math.max(1L, intervalSeconds * 20L);
        sampleTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::sample, 0L, ticks);
    }

    @Override
    public void stop()
    {
        if (sampleTask != null) {
            sampleTask.cancel();
            sampleTask = null;
        }
    }

    private void sample()
    {
        var totalChunks   = 0;
        var totalEntities = 0;
        var notable       = new ArrayList<WorldMetrics>();

        for (var world : plugin.getServer().getWorlds()) {
            var chunks   = world.getLoadedChunks().length;
            var entities = world.getEntities().size();
            totalChunks += chunks;
            totalEntities += entities;
            if (WorldMetrics.isNotable(chunks, entities)) {
                notable.add(new WorldMetrics(world.getName(), chunks, entities));
            }
        }

        latest.set(new WorldStatsSnapshot(totalChunks, totalEntities, notable));
    }
}
