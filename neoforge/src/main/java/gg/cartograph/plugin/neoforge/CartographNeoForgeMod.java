package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.TickSampler;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.*;
import gg.cartograph.plugin.common.logging.Log4jCartographLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * NeoForge mod entry point for the Cartograph plugin.
 *
 * <p>Registers the config spec with NeoForge's configuration system at construction
 * time, then loads the resolved values into a {@link CartographConfig} when the server
 * starts and constructs the {@link Cartograph} runtime.</p>
 *
 * @see NeoForgeConfigLoader
 */
@Mod("cartograph")
public class CartographNeoForgeMod
{

    private static final Logger LOGGER = LogManager.getLogger();

    private CartographConfig cartographConfig;

    private Cartograph cartograph;

    private TickSampler tickSampler;

    private net.minecraft.server.MinecraftServer minecraftServer;

    public CartographNeoForgeMod(IEventBus modBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigLoader.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        cartographConfig = NeoForgeConfigLoader.load();
        tickSampler      = new TickSampler();
        minecraftServer  = event.getServer();
        cartograph       = new Cartograph(cartographConfig, new Log4jCartographLogger(LOGGER), this::buildHeartbeat);
        cartograph.start();
        cartograph.record(buildBootEvent(event));
    }

    private HeartbeatTelemetryEvent buildHeartbeat()
    {
        var runtime = Runtime.getRuntime();
        var osBean = (com.sun.management.OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();

        var meanTick = tickSampler.getMeanTickTime();
        var peakTick = tickSampler.getPeakTickTime();
        tickSampler.reset();

        var chunksLoaded = 0;
        var worlds       = new java.util.ArrayList<WorldMetrics>();

        for (var level : minecraftServer.getAllLevels()) {
            var chunks = level.getChunkSource().getLoadedChunksCount();
            chunksLoaded += chunks;
            if (WorldMetrics.isNotable(chunks, 0)) {
                worlds.add(new WorldMetrics(
                        level.dimension().location().toString(),
                        chunks,
                        null
                ));
            }
        }

        return new HeartbeatTelemetryEvent(
                System.currentTimeMillis(),
                new double[] {minecraftServer.getAverageTickTimeNanos() / 1_000_000.0},
                meanTick,
                peakTick,
                minecraftServer.getPlayerCount(),
                runtime.totalMemory() - runtime.freeMemory(),
                runtime.maxMemory(),
                osBean.getProcessCpuLoad(),
                osBean.getCpuLoad(),
                Thread.activeCount(),
                chunksLoaded,
                null,
                worlds.isEmpty() ? null : worlds
        );
    }

    private BootTelemetryEvent buildBootEvent(ServerStartingEvent event)
    {
        var server = event.getServer();

        var mods = ModList.get().getMods().stream()
                          .map(mod -> new PluginInfo(mod.getModId(), mod.getVersion().toString(), true))
                          .toList();

        var worlds    = server.getAllLevels().spliterator();
        var worldList = new java.util.ArrayList<WorldInfo>();
        worlds.forEachRemaining(level -> worldList.add(
                new WorldInfo(level.dimension().location().toString(), level.dimension().location().getPath())
        ));

        var neoforgeVersion = ModList.get().getModContainerById("neoforge")
                                     .map(c -> c.getModInfo().getVersion().toString())
                                     .orElse("unknown");

        return new BootTelemetryEvent(
                System.currentTimeMillis(),
                "NeoForge",
                neoforgeVersion,
                server.getServerVersion(),
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                new OsInfo(
                        System.getProperty("os.name"),
                        System.getProperty("os.version"),
                        System.getProperty("os.arch")
                ),
                ModList.get().getModContainerById("cartograph")
                       .map(c -> c.getModInfo().getVersion().toString())
                       .orElse("unknown"),
                cartograph.isProxyBackend() ? NodeType.BACKEND : NodeType.STANDALONE, // no platform detection available for NeoForge
                server.getMaxPlayers(),
                server.getPlayerList().getViewDistance(),
                server.getPlayerList().getSimulationDistance(),
                server.usesAuthentication(),
                null,
                server.getMotd(),
                cartograph.shouldReportPlugins() ? mods : null,
                null,
                worldList,
                List.of()
        );
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        if (cartograph != null) {
            cartograph.record(new ShutdownTelemetryEvent(
                    System.currentTimeMillis(),
                    cartograph.getUptime(),
                    ShutdownReason.CLEAN
            ));
            cartograph.stop();
        }
    }

    @SubscribeEvent
    public void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event)
    {
        if (tickSampler != null && minecraftServer != null) {
            tickSampler.recordTick(minecraftServer.getAverageTickTimeNanos() / 1_000_000.0);
        }
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }

    public Cartograph getCartograph()
    {
        return cartograph;
    }
}
