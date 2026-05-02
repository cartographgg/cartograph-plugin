package gg.cartograph.plugin.folia;

import gg.cartograph.plugin.common.events.WorldMetrics;
import gg.cartograph.plugin.common.world.WorldStatsProvider;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Folia world stats provider.
 *
 * <p>Folia's region-threaded model has no single "main thread" — entities are
 * owned by their containing region's scheduler. This provider samples on the
 * <em>global</em> region scheduler ({@link Bukkit#getGlobalRegionScheduler()}),
 * which is suitable for global server state. We sample {@code World#getLoadedChunks()}
 * here because it is documented as safe to call from the global scheduler;
 * we do <strong>not</strong> sample entity counts because iterating entities
 * across regions requires per-region scheduling, which would significantly
 * complicate the heartbeat path.</p>
 *
 * <p>Heartbeat events from Folia therefore have {@code entitiesLoaded == null}
 * (omitted from the wire payload) and per-world {@code entitiesLoaded} set to
 * {@code null}. {@code chunksLoaded} is reported normally.</p>
 */
public class FoliaWorldStatsProvider implements WorldStatsProvider
{

    private final JavaPlugin plugin;

    private final AtomicReference<WorldStatsSnapshot> latest = new AtomicReference<>(WorldStatsSnapshot.EMPTY);

    private ScheduledTask sampleTask;

    public FoliaWorldStatsProvider(JavaPlugin plugin)
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
        sampleTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> sample(), 1L, ticks);
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
        var totalChunks = 0;
        var notable     = new ArrayList<WorldMetrics>();

        for (var world : plugin.getServer().getWorlds()) {
            var chunks = world.getLoadedChunks().length;
            totalChunks += chunks;
            if (WorldMetrics.isNotable(chunks, 0)) {
                notable.add(new WorldMetrics(world.getName(), chunks, null));
            }
        }

        latest.set(new WorldStatsSnapshot(totalChunks, null, notable));
    }
}
