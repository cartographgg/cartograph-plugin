package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.JulCartographLogger;
import gg.cartograph.plugin.common.config.CartographConfig;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

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
    }

    @Override
    public void onDisable()
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
}
