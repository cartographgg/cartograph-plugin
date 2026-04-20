package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.BootTelemetryEvent;
import gg.cartograph.plugin.common.events.OsInfo;
import gg.cartograph.plugin.common.events.PluginInfo;
import gg.cartograph.plugin.common.events.WorldInfo;
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

    public CartographNeoForgeMod(IEventBus modBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigLoader.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        cartographConfig = NeoForgeConfigLoader.load();
        cartograph       = new Cartograph(cartographConfig, new Log4jCartographLogger(LOGGER));
        cartograph.start();
        cartograph.record(buildBootEvent(event));
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        if (cartograph != null) {
            cartograph.stop();
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

    private BootTelemetryEvent buildBootEvent(ServerStartingEvent event)
    {
        var server = event.getServer();

        var mods = ModList.get().getMods().stream()
                .map(mod -> new PluginInfo(mod.getModId(), mod.getVersion().toString(), true))
                .toList();

        var worlds = server.getAllLevels().spliterator();
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
                NodeType.STANDALONE,
                server.getMaxPlayers(),
                server.getPlayerList().getViewDistance(),
                server.getPlayerList().getSimulationDistance(),
                server.usesAuthentication(),
                null,
                server.getMotd(),
                mods,
                null,
                worldList,
                List.of()
        );
    }
}
