package gg.cartograph.plugin.folia;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;

/**
 * Folia entry point for the Cartograph plugin.
 *
 * <p>All configuration loading and lifecycle management is handled by
 * {@link CartographBukkitPlugin}. This class provides Folia-specific
 * initialisation only.</p>
 */
public class CartographFoliaPlugin extends CartographBukkitPlugin
{

    @Override
    public void enable()
    {
        getLogger().info("Cartograph enabled (Folia)");
    }

    @Override
    public void disable()
    {
        getLogger().info("Cartograph disabled (Folia)");
    }
}
