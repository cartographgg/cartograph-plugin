package gg.cartograph.plugin.spigot;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;

/**
 * Spigot entry point for the Cartograph plugin.
 *
 * <p>All configuration loading and lifecycle management is handled by
 * {@link CartographBukkitPlugin}. This class provides Spigot-specific
 * initialisation only.</p>
 */
public class CartographSpigotPlugin extends CartographBukkitPlugin
{

    @Override
    public void enable()
    {
        getLogger().info("Cartograph enabled (Spigot)");
    }

    @Override
    public void disable()
    {
        getLogger().info("Cartograph disabled (Spigot)");
    }
}
