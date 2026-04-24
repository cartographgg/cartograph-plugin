package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.*;
import gg.cartograph.plugin.common.events.telemetry.BootTelemetryEvent;
import gg.cartograph.plugin.common.events.telemetry.HeartbeatTelemetryEvent;
import gg.cartograph.plugin.common.events.telemetry.ShutdownTelemetryEvent;
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
        if (cartographConfig.getIpHashSalt().isEmpty()) {
            var bytes = new byte[32];
            new java.security.SecureRandom().nextBytes(bytes);
            var salt = java.util.HexFormat.of().formatHex(bytes);
            cartographConfig.setIpHashSalt(salt);
            getConfig().set("ip-hash-salt", salt);
            saveConfig();
        }
        cartograph  = new Cartograph(cartographConfig, new JulCartographLogger(getLogger()), this::buildHeartbeat);
        cartograph.start();
        cartograph.record(buildBootEvent());
        startTickSampling();
        if (!cartograph.isProxyBackend()) {
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(cartograph), this);
            getServer().getPluginManager().registerEvents(new PlayerLeaveListener(cartograph), this);
        }
    }

    private HeartbeatTelemetryEvent buildHeartbeat()
    {
        var server  = getServer();
        var runtime = Runtime.getRuntime();
        var osBean = (com.sun.management.OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();

        var meanTick = cartograph.getTickSampler().getMeanTickTime();
        var peakTick = cartograph.getTickSampler().getPeakTickTime();
        cartograph.getTickSampler().reset();

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

        var resourcePacks = new java.util.ArrayList<ResourcePackInfo>();
        @SuppressWarnings("deprecation") var rpUrl = server.getResourcePack();
        if (rpUrl != null && !rpUrl.isEmpty()) {
            @SuppressWarnings("deprecation") var rpHash = server.getResourcePackHash();
            resourcePacks.add(new ResourcePackInfo(
                    rpUrl,
                    rpHash.isEmpty() ? null : rpHash,
                    server.isResourcePackRequired(),
                    null
            ));
        }

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
                List.of(),
                resourcePacks.isEmpty() ? null : resourcePacks,
                null
        );
    }

    private void startTickSampling()
    {
        final long[] lastTick = {System.nanoTime()};
        getServer().getScheduler().runTaskTimer(
                this, () -> {
                    var now     = System.nanoTime();
                    var elapsed = (now - lastTick[0]) / 1_000_000.0;
                    lastTick[0] = now;
                    cartograph.getTickSampler().recordTick(elapsed);
                }, 1L, 1L
        );
    }

    protected double[] getTps()
    {
        return cartograph.getTickSampler().getTps();
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
