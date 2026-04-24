package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.BackendInfo;
import gg.cartograph.plugin.common.events.OsInfo;
import gg.cartograph.plugin.common.events.PluginInfo;
import gg.cartograph.plugin.common.events.ShutdownReason;
import gg.cartograph.plugin.common.events.telemetry.BootTelemetryEvent;
import gg.cartograph.plugin.common.events.telemetry.HeartbeatTelemetryEvent;
import gg.cartograph.plugin.common.events.telemetry.ShutdownTelemetryEvent;
import gg.cartograph.plugin.common.logging.JulCartographLogger;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.lang.management.ManagementFactory;
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
        if (cartographConfig.getIpHashSalt().isEmpty()) {
            var bytes = new byte[32];
            new java.security.SecureRandom().nextBytes(bytes);
            var salt = java.util.HexFormat.of().formatHex(bytes);
            cartographConfig.setIpHashSalt(salt);
            try {
                var configFile = new java.io.File(getDataFolder(), "config.yml");
                var yaml = net.md_5.bungee.config.ConfigurationProvider.getProvider(
                        net.md_5.bungee.config.YamlConfiguration.class
                ).load(configFile);
                yaml.set("ip-hash-salt", salt);
                net.md_5.bungee.config.ConfigurationProvider.getProvider(
                        net.md_5.bungee.config.YamlConfiguration.class
                ).save(yaml, configFile);
            } catch (IOException e) {
                getLogger().warning("Failed to save generated ip-hash-salt: " + e.getMessage());
            }
        }
        cartograph = new Cartograph(cartographConfig, new JulCartographLogger(getLogger()), this::buildHeartbeat);
        cartograph.start();
        cartograph.record(buildBootEvent());
        getProxy().getPluginManager().registerListener(this, new PlayerJoinListener(cartograph));
        getProxy().getPluginManager().registerListener(this, new PlayerLeaveListener(cartograph));
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

    private HeartbeatTelemetryEvent buildHeartbeat()
    {
        var runtime = Runtime.getRuntime();
        var osBean = (com.sun.management.OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();

        return new HeartbeatTelemetryEvent(
                System.currentTimeMillis(),
                null,
                null,
                null,
                getProxy().getOnlineCount(),
                runtime.totalMemory() - runtime.freeMemory(),
                runtime.maxMemory(),
                osBean.getProcessCpuLoad(),
                osBean.getCpuLoad(),
                Thread.activeCount(),
                null,
                null,
                null
        );
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
                cartograph.shouldReportPlugins() ? plugins : null,
                backends,
                null,
                List.of(),
                null,
                null
        );
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
