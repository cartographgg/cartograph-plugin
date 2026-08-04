package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.detection.BootCapabilities;
import gg.cartograph.plugin.common.events.*;
import gg.cartograph.plugin.common.events.telemetry.BootTelemetryEvent;
import gg.cartograph.plugin.common.events.telemetry.HeartbeatTelemetryEvent;
import gg.cartograph.plugin.common.events.telemetry.ShutdownTelemetryEvent;
import gg.cartograph.plugin.common.logging.Log4jCartographLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ServiceLoader;

/**
 * NeoForge mod entry point for the Cartograph plugin.
 *
 * <p>Version-agnostic: it references only {@code net.neoforged} API and holds
 * the {@code net.minecraft} server as an {@link Object}, delegating every
 * {@code net.minecraft} operation to a {@link NeoForgePlatform} impl resolved at
 * startup from the installed version module via {@link ServiceLoader}.</p>
 *
 * @see NeoForgeConfigLoader
 * @see NeoForgePlatform
 */
@Mod("cartograph")
public class CartographNeoForgeMod
{

    private static final Logger LOGGER = LogManager.getLogger();

    private CartographConfig cartographConfig;

    private Cartograph cartograph;

    private NeoForgePlatform platform;

    private Object minecraftServer;

    private final NeoForgeWorldStatsProvider worldStats = new NeoForgeWorldStatsProvider();

    public CartographNeoForgeMod(IEventBus modBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigLoader.SPEC);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        cartographConfig = NeoForgeConfigLoader.load();
        platform         = ServiceLoader.load(NeoForgePlatform.class, getClass().getClassLoader())
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalStateException(
                                                "No NeoForgePlatform impl on the classpath — the version module is missing"));
        minecraftServer  = event.getServer();
        cartograph       = new Cartograph(cartographConfig, new Log4jCartographLogger(LOGGER), this::collectHeartbeat);
        cartograph.start();
        var heartbeatConfig = cartographConfig.getTelemetry().get("heartbeat");
        if (heartbeatConfig != null && heartbeatConfig.isEnabled()) {
            worldStats.start(heartbeatConfig.getInterval());
        }
        cartograph.record(buildBootEvent());
        NeoForge.EVENT_BUS.register(new PlayerJoinListener(cartograph, platform));
        NeoForge.EVENT_BUS.register(new PlayerLeaveListener(cartograph, platform));
    }

    private HeartbeatTelemetryEvent collectHeartbeat()
    {
        var pct = cartograph.getTickSampler().getPercentiles();
        cartograph.getTickSampler().reset();

        var heartbeat = cartographConfig.getTelemetry().get("heartbeat");
        var interval  = heartbeat != null ? heartbeat.getInterval() : null;

        return NeoForgeHeartbeat.build(platform, minecraftServer, interval, pct, worldStats.snapshot());
    }

    private BootTelemetryEvent buildBootEvent()
    {
        var mods = ModList.get().getMods().stream()
                          .map(mod -> new ModInfo(mod.getModId(), mod.getDisplayName(), mod.getVersion().toString()))
                          .toList();

        var neoforgeVersion = ModList.get().getModContainerById("neoforge")
                                     .map(c -> c.getModInfo().getVersion().toString())
                                     .orElse("unknown");

        var cartographVersion = ModList.get().getModContainerById("cartograph")
                                       .map(c -> c.getModInfo().getVersion().toString())
                                       .orElse("unknown");

        return new BootTelemetryEvent(
                System.currentTimeMillis(),
                "NeoForge",
                neoforgeVersion,
                platform.serverVersion(minecraftServer),
                System.getProperty("java.version"),
                new OsInfo(System.getProperty("os.name"), System.getProperty("os.arch")),
                cartographVersion,
                cartograph.isProxyBackend() ? NodeType.BACKEND : NodeType.STANDALONE,
                platform.maxPlayers(minecraftServer),
                platform.viewDistance(minecraftServer),
                platform.simulationDistance(minecraftServer),
                platform.usesAuthentication(minecraftServer),
                null,
                null,
                null,
                platform.worlds(minecraftServer),
                cartograph.shouldReportPlugins() ? mods : null,
                BootCapabilities.detectClientVersion(cartograph.getLogger()),
                BootCapabilities.detectBedrockSupport(cartograph.getLogger())
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
            worldStats.stop();
            cartograph.stop();
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event)
    {
        if (cartograph != null && minecraftServer != null) {
            cartograph.getTickSampler().recordTick(platform.averageTickTimeMs(minecraftServer));
            worldStats.sample(platform, minecraftServer);
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
