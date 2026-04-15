package gg.cartograph.plugin.spigot;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;

public class CartographSpigotPlugin extends CartographBukkitPlugin {

    @Override
    public void enable() {
        getLogger().info("Cartograph enabled (Spigot)");
    }

    @Override
    public void disable() {
        getLogger().info("Cartograph disabled (Spigot)");
    }
}
