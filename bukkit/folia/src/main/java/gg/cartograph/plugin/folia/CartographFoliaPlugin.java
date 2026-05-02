package gg.cartograph.plugin.folia;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;
import gg.cartograph.plugin.common.world.WorldStatsProvider;
import org.bukkit.Bukkit;

/**
 * Folia entry point for the Cartograph plugin.
 *
 * <p>All configuration loading and lifecycle management is handled by
 * {@link CartographBukkitPlugin}. Folia overrides:</p>
 * <ul>
 *   <li>world-stats sampling — uses the global region scheduler instead of
 *       the main-thread Bukkit scheduler;</li>
 *   <li>tick sampling — Folia rejects the legacy {@code BukkitScheduler.runTaskTimer}
 *       API, so we sample on the global region scheduler at a 1-tick interval.</li>
 * </ul>
 */
public class CartographFoliaPlugin extends CartographBukkitPlugin
{

    // Note: getTps() is intentionally NOT overridden. CraftServer#getTPS() throws
    // UnsupportedOperationException on Folia ("Not on any region"). The parent
    // class falls back to our internal TickSampler, which is fed by the
    // tick-sampling task we install on the global region scheduler in
    // startTickSampling().

    @Override
    protected WorldStatsProvider createWorldStatsProvider()
    {
        return new FoliaWorldStatsProvider(this);
    }

    @Override
    protected void startTickSampling()
    {
        final long[] lastTick = {System.nanoTime()};
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> {
            var now     = System.nanoTime();
            var elapsed = (now - lastTick[0]) / 1_000_000.0;
            lastTick[0] = now;
            getCartograph().getTickSampler().recordTick(elapsed);
        }, 1L, 1L);
    }
}
