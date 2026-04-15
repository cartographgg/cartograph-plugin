package gg.cartograph.plugin.paper;

import gg.cartograph.plugin.bukkit.CartographBukkitPlugin;

public class CartographPaperPlugin extends CartographBukkitPlugin {

    @Override
    public void enable() {
        getLogger().info("Cartograph enabled (Paper)");
    }

    @Override
    public void disable() {
        getLogger().info("Cartograph disabled (Paper)");
    }
}
