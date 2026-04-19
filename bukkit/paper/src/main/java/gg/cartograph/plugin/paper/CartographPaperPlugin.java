package gg.cartograph.plugin.paper;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;

/**
 * Paper entry point for the Cartograph plugin.
 *
 * <p>All configuration loading and lifecycle management is handled by
 * {@link CartographBukkitPlugin}. This class provides Paper-specific
 * initialisation only.</p>
 */
public class CartographPaperPlugin extends CartographBukkitPlugin
{

    @Override
    public void enable()
    {
        getLogger().info("Cartograph enabled (Paper)");
    }

    @Override
    public void disable()
    {
        getLogger().info("Cartograph disabled (Paper)");
    }
}
