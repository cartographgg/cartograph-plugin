package gg.cartograph.plugin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import javax.inject.Inject;

@Plugin(
    id = "cartograph",
    name = "Cartograph",
    version = "1.0.0-SNAPSHOT",
    description = "Cartograph metrics plugin",
    authors = {"Cartograph"}
)
public class CartographVelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public CartographVelocityPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Cartograph enabled (Velocity)");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Cartograph disabled (Velocity)");
    }
}
