package gg.cartograph.plugin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.cartograph.plugin.common.config.CartographConfig;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Velocity proxy entry point for the Cartograph plugin.
 *
 * <p>Uses Velocity's dependency injection to receive the proxy server, logger, and
 * data directory. Configuration is loaded from {@code config.yml} in the plugin's
 * data directory when the proxy initialises. If the config cannot be read, the plugin
 * logs an error and does not start — no telemetry will be collected or transmitted.</p>
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

    private final Logger      logger;

    private final Path        dataDirectory;

    private CartographConfig  cartographConfig;

    @Inject
    public CartographVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory)
    {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event)
    {
        try
        {
            cartographConfig = VelocityConfigLoader.load(dataDirectory);
        }
        catch (IOException e)
        {
            logger.error("Failed to load config", e);
            return;
        }
        logger.info("Cartograph enabled (Velocity)");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event)
    {
        logger.info("Cartograph disabled (Velocity)");
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }
}
