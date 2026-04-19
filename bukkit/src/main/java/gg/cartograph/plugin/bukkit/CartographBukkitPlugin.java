package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.CartographPlugin;
import gg.cartograph.plugin.common.config.CartographConfig;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Base class for all Bukkit-derived server implementations (Spigot, Paper, Folia).
 *
 * <p>Handles the shared Bukkit lifecycle: loads configuration from {@code config.yml}
 * on enable, then delegates to the platform-specific {@link #enable()} and
 * {@link #disable()} hooks. Each server variant (Spigot, Paper, Folia) extends this
 * class and provides its own implementation of those hooks.</p>
 *
 * <p>Configuration is read once on startup via {@link BukkitConfigLoader} and stored
 * in memory. No config values are written back to disk by the plugin.</p>
 *
 * @see BukkitConfigLoader
 */
public abstract class CartographBukkitPlugin extends JavaPlugin implements CartographPlugin
{

    private CartographConfig cartographConfig;

    @Override
    public void onEnable()
    {
        cartographConfig = BukkitConfigLoader.load(this);
        enable();
    }

    @Override
    public void onDisable()
    {
        disable();
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }
}
