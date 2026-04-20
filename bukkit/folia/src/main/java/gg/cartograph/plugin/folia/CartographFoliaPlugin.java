package gg.cartograph.plugin.folia;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;

/**
 * Folia entry point for the Cartograph plugin.
 *
 * <p>All configuration loading and lifecycle management is handled by
 * {@link CartographBukkitPlugin}.</p>
 */
public class CartographFoliaPlugin extends CartographBukkitPlugin
{

    @Override
    protected double[] getTps()
    {
        return getServer().getTPS();
    }
}
