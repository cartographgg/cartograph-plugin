package gg.cartograph.plugin.paper;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;

/**
 * Paper entry point for the Cartograph plugin.
 *
 * <p>All configuration loading and lifecycle management is handled by
 * {@link CartographBukkitPlugin}.</p>
 */
public class CartographPaperPlugin extends CartographBukkitPlugin
{

    @Override
    protected double[] getTps()
    {
        return getServer().getTPS();
    }
}
