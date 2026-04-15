package gg.cartograph.plugin.folia;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;

public class CartographFoliaPlugin extends CartographBukkitPlugin {

    @Override
    public void enable() {
        getLogger().info("Cartograph enabled (Folia)");
    }

    @Override
    public void disable() {
        getLogger().info("Cartograph disabled (Folia)");
    }
}
