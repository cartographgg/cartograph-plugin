package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.CartographPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CartographBukkitPlugin extends JavaPlugin implements CartographPlugin {

    @Override
    public void onEnable() {
        enable();
    }

    @Override
    public void onDisable() {
        disable();
    }
}
