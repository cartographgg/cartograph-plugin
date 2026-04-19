package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.config.CartographConfig;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

/**
 * BungeeCord proxy entry point for the Cartograph plugin.
 *
 * <p>Loads configuration from {@code plugins/Cartograph/config.yml} on enable.
 * If the config file cannot be read, the plugin logs an error and does not
 * start — no telemetry will be collected or transmitted in that case.</p>
 *
 * @see BungeeConfigLoader
 */
public class CartographBungeePlugin extends Plugin
{

    private CartographConfig cartographConfig;

    @Override
    public void onEnable()
    {
        try
        {
            cartographConfig = BungeeConfigLoader.load(this);
        }
        catch (IOException e)
        {
            getLogger().severe("Failed to load config: " + e.getMessage());
            return;
        }
        getLogger().info("Cartograph enabled (BungeeCord)");
    }

    @Override
    public void onDisable()
    {
        getLogger().info("Cartograph disabled (BungeeCord)");
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }
}
