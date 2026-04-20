package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.TickSampler;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.*;
import gg.cartograph.plugin.common.logging.JulCartographLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for all Bukkit-derived server implementations (Spigot, Paper, Folia).
 *
 * <p>Handles the shared Bukkit lifecycle: loads configuration from {@code config.yml}
 * on enable, constructs the {@link Cartograph} runtime.</p>
 *
 * @see BukkitConfigLoader
 */
public abstract class CartographBukkitPlugin extends JavaPlugin
{

    private CartographConfig cartographConfig;

    private Cartograph cartograph;

    private TickSampler tickSampler;

    @Override
    public void onDisable()
    {
        cartograph.record(new ShutdownTelemetryEvent(
                System.currentTimeMillis(),
                cartograph.getUptime(),
                ShutdownReason.CLEAN
        ));
        cartograph.stop();
    }

    @Override
    public void onEnable()
    {
        cartographConfig = BukkitConfigLoader.load(this);
        tickSampler      = new TickSampler();
        cartograph       = new Cartograph(cartographConfig, new JulCartographLogger(getLogger()), this::buildHeartbeat);
        cartograph.start();
        cartograph.record(buildBootEvent());
        startTickSampling();
    }

    private HeartbeatTelemetryEvent buildHeartbeat()
    {
        var server  = getServer();
        var runtime = Runtime.getRuntime();
        var osBean = (com.sun.management.OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();

        var meanTick = tickSampler.getMeanTickTime();
        var peakTick = tickSampler.getPeakTickTime();
        tickSampler.reset();

        var chunksLoaded   = 0;
        var entitiesLoaded = 0;
        var worlds         = new java.util.ArrayList<WorldMetrics>();

        for (var world : server.getWorlds()) {
            var chunks   = world.getLoadedChunks().length;
            var entities = world.getEntities().size();
            chunksLoaded += chunks;
            entitiesLoaded += entities;
            if (WorldMetrics.isNotable(chunks, entities)) {
                worlds.add(new WorldMetrics(world.getName(), chunks, entities));
            }
        }

        return new HeartbeatTelemetryEvent(
                System.currentTimeMillis(),
                getTps(),
                meanTick,
                peakTick,
                server.getOnlinePlayers().size(),
                runtime.totalMemory() - runtime.freeMemory(),
                runtime.maxMemory(),
                osBean.getProcessCpuLoad(),
                osBean.getCpuLoad(),
                Thread.activeCount(),
                chunksLoaded,
                entitiesLoaded,
                worlds.isEmpty() ? null : worlds
        );
    }

    private BootTelemetryEvent buildBootEvent()
    {
        var server = getServer();

        var plugins = Arrays.stream(server.getPluginManager().getPlugins())
                            .map(p -> new PluginInfo(p.getName(), p.getDescription().getVersion(), p.isEnabled()))
                            .toList();

        var worlds = server.getWorlds().stream()
                           .map(w -> new WorldInfo(w.getName(), w.getEnvironment().name().toLowerCase()))
                           .toList();

        return new BootTelemetryEvent(
                System.currentTimeMillis(),
                server.getName(),
                server.getVersion(),
                server.getBukkitVersion(),
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                new OsInfo(
                        System.getProperty("os.name"),
                        System.getProperty("os.version"),
                        System.getProperty("os.arch")
                ),
                getDescription().getVersion(),
                detectNodeType(),
                server.getMaxPlayers(),
                server.getViewDistance(),
                server.getSimulationDistance(),
                server.getOnlineMode(),
                server.hasWhitelist(),
                server.getMotd(),
                cartograph.shouldReportPlugins() ? plugins : null,
                null,
                worlds,
                List.of()
        );
    }

    private void startTickSampling()
    {
        // Schedule a task every tick to measure actual tick duration
        final long[] lastTick = {System.nanoTime()};
        getServer().getScheduler().runTaskTimer(
                this, () -> {
                    var now     = System.nanoTime();
                    var elapsed = (now - lastTick[0]) / 1_000_000.0;
                    lastTick[0] = now;
                    tickSampler.recordTick(elapsed);
                }, 1L, 1L
        );
    }

    protected double[] getTps()
    {
        return tickSampler.getTps();
    }

    private NodeType detectNodeType()
    {
        if (cartograph.isProxyBackend()) {
            return NodeType.BACKEND;
        }

        try {
            var spigotConfig = getServer().spigot().getConfig();
            if (spigotConfig.getBoolean("settings.bungeecord", false)) {
                return NodeType.BACKEND;
            }
        } catch (Exception ignored) {
        }

        return NodeType.STANDALONE;
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }

    protected Cartograph getCartograph()
    {
        return cartograph;
    }
}
