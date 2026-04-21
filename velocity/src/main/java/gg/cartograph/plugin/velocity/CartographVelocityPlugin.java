package gg.cartograph.plugin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.*;
import gg.cartograph.plugin.common.logging.Slf4jCartographLogger;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Velocity proxy entry point for the Cartograph plugin.
 *
 * <p>Uses Velocity's dependency injection to receive the proxy server, logger, and
 * data directory. Configuration is loaded from {@code config.yml} in the plugin's
 * data directory when the proxy initialises.</p>
 *
 * @see VelocityConfigLoader
 */
@Plugin(
        id = "cartograph",
        name = "Cartograph",
        version = "1.0.0-SNAPSHOT",
        description = "Cartograph metrics plugin",
        authors = {"Cartograph"}
)
public class CartographVelocityPlugin
{

    private final ProxyServer server;

    private final Logger logger;

    private final Path dataDirectory;

    private CartographConfig cartographConfig;

    private Cartograph cartograph;

    @Inject
    public CartographVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory)
    {
        this.server        = server;
        this.logger        = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event)
    {
        try {
            cartographConfig = VelocityConfigLoader.load(dataDirectory);
        } catch (IOException e) {
            logger.error("Failed to load config", e);
            return;
        }
        if (cartographConfig.getIpHashSalt().isEmpty()) {
            var bytes = new byte[32];
            new java.security.SecureRandom().nextBytes(bytes);
            var salt = java.util.HexFormat.of().formatHex(bytes);
            cartographConfig.setIpHashSalt(salt);
            try {
                var                 configPath = dataDirectory.resolve("config.yml");
                var                 yaml       = new org.yaml.snakeyaml.Yaml();
                Map<String, Object> data;
                try (var reader = java.nio.file.Files.newBufferedReader(configPath)) {
                    data = yaml.load(reader);
                }
                if (data == null) {
                    data = new java.util.LinkedHashMap<>();
                }
                data.put("ip-hash-salt", salt);
                try (var writer = java.nio.file.Files.newBufferedWriter(configPath)) {
                    yaml.dump(data, writer);
                }
            } catch (IOException ex) {
                logger.warn("Failed to save generated ip-hash-salt: {}", ex.getMessage());
            }
        }
        cartograph = new Cartograph(cartographConfig, new Slf4jCartographLogger(logger), this::buildHeartbeat);
        cartograph.start();
        cartograph.record(buildBootEvent());
        server.getEventManager().register(this, new PlayerJoinListener(cartograph));
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
                server.getPlayerCount(),
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
        var version = server.getVersion();

        var plugins = server.getPluginManager().getPlugins().stream()
                            .map(PluginContainer::getDescription)
                            .map(d -> new PluginInfo(
                                    d.getId(),
                                    d.getVersion().orElse("unknown"),
                                    true
                            ))
                            .toList();

        var backends = server.getAllServers().stream()
                             .map(s -> {
                                 var info = s.getServerInfo();
                                 var addr = info.getAddress();
                                 return new BackendInfo(info.getName(), addr.getHostString() + ":" + addr.getPort());
                             })
                             .toList();

        return new BootTelemetryEvent(
                System.currentTimeMillis(),
                version.getName(),
                version.getVersion(),
                null,
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                new OsInfo(
                        System.getProperty("os.name"),
                        System.getProperty("os.version"),
                        System.getProperty("os.arch")
                ),
                server.getPluginManager().getPlugin("cartograph")
                      .flatMap(p -> p.getDescription().getVersion())
                      .orElse("unknown"),
                NodeType.PROXY,
                server.getConfiguration().getShowMaxPlayers(),
                null,
                null,
                server.getConfiguration().isOnlineMode(),
                null,
                null,
                cartograph.shouldReportPlugins() ? plugins : null,
                backends,
                null,
                List.of()
        );
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event)
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
}
