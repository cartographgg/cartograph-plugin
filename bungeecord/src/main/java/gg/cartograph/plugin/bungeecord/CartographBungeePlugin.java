package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.BackendInfo;
import gg.cartograph.plugin.common.events.BootTelemetryEvent;
import gg.cartograph.plugin.common.events.OsInfo;
import gg.cartograph.plugin.common.events.PluginInfo;
import gg.cartograph.plugin.common.events.ShutdownReason;
import gg.cartograph.plugin.common.events.ShutdownTelemetryEvent;
import gg.cartograph.plugin.common.logging.JulCartographLogger;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.util.List;

/**
 * BungeeCord proxy entry point for the Cartograph plugin.
 *
 * <p>Loads configuration from {@code plugins/Cartograph/config.yml} on enable and
 * constructs the {@link Cartograph} runtime. If the config file cannot be read,
 * the plugin logs an error and does not start.</p>
 *
 * @see BungeeConfigLoader
 */
public class CartographBungeePlugin extends Plugin
{

    private CartographConfig cartographConfig;

    private Cartograph cartograph;

    @Override
    public void onEnable()
    {
        try {
            cartographConfig = BungeeConfigLoader.load(this);
        } catch (IOException e) {
            getLogger().severe("Failed to load config: " + e.getMessage());
            return;
        }
        cartograph = new Cartograph(cartographConfig, new JulCartographLogger(getLogger()));
        cartograph.start();
        cartograph.record(buildBootEvent());
    }

    @Override
    public void onDisable()
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

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }

    public Cartograph getCartograph()
    {
        return cartograph;
    }

    private BootTelemetryEvent buildBootEvent()
    {
        var proxy = getProxy();

        var plugins = proxy.getPluginManager().getPlugins().stream()
                           .map(p -> new PluginInfo(
                                   p.getDescription().getName(),
                                   p.getDescription().getVersion(),
                                   true
                           ))
                           .toList();

        var backends = proxy.getServers().values().stream()
                           .map(s -> {
                               var addr = s.getSocketAddress();
                               return new BackendInfo(s.getName(), addr.toString());
                           })
                           .toList();

        return new BootTelemetryEvent(
                System.currentTimeMillis(),
                proxy.getName(),
                proxy.getVersion(),
                null,
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                new OsInfo(
                        System.getProperty("os.name"),
                        System.getProperty("os.version"),
                        System.getProperty("os.arch")
                ),
                getDescription().getVersion(),
                NodeType.PROXY,
                proxy.getConfig().getPlayerLimit(),
                null,
                null,
                proxy.getConfig().isOnlineMode(),
                null,
                null,
                plugins,
                backends,
                null,
                List.of()
        );
    }
}
