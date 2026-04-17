package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.CartographPlugin;
import gg.cartograph.plugin.common.config.CartographConfig;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CartographBukkitPlugin extends JavaPlugin implements CartographPlugin
{

    private CartographConfig cartographConfig;

    @Override
    public void onEnable()
    {
        cartographConfig = BukkitConfigLoader.load(this);
        enable();
    }

    @Override
    public void onDisable()
    {
        disable();
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }
}
