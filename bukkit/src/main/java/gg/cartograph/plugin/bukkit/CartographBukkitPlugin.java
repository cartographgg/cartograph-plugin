package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.*;
import gg.cartograph.plugin.common.logging.JulCartographLogger;
import org.bukkit.plugin.java.JavaPlugin;

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
        cartograph       = new Cartograph(cartographConfig, new JulCartographLogger(getLogger()));
        cartograph.start();
        cartograph.record(buildBootEvent());
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }

    protected Cartograph getCartograph()
    {
        return cartograph;
    }

    private NodeType detectNodeType()
    {
        try {
            var spigotConfig = getServer().spigot().getConfig();
            if (spigotConfig.getBoolean("settings.bungeecord", false)) {
                return NodeType.BACKEND;
            }
        } catch (Exception ignored) {
            // spigot() config may not be available on all implementations
        }

        return NodeType.STANDALONE;
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
                plugins,
                null,
                worlds,
                List.of()
        );
    }
}
