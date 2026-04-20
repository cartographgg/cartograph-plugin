package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.CartographPlugin;
import gg.cartograph.plugin.common.JulCartographLogger;
import gg.cartograph.plugin.common.config.CartographConfig;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Base class for all Bukkit-derived server implementations (Spigot, Paper, Folia).
 *
 * <p>Handles the shared Bukkit lifecycle: loads configuration from {@code config.yml}
 * on enable, constructs the {@link Cartograph} runtime, then delegates to the
 * platform-specific {@link #enable()} and {@link #disable()} hooks.</p>
 *
 * @see BukkitConfigLoader
 */
public abstract class CartographBukkitPlugin extends JavaPlugin implements CartographPlugin
{

    private CartographConfig cartographConfig;

    private Cartograph cartograph;

    @Override
    public void onDisable()
    {
        disable();
        cartograph.stop();
    }

    @Override
    public void onEnable()
    {
        cartographConfig = BukkitConfigLoader.load(this);
        cartograph       = new Cartograph(cartographConfig, new JulCartographLogger(getLogger()));
        cartograph.start();
        enable();
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }

    protected Cartograph getCartograph()
    {
        return cartograph;
    }
}
