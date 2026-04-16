package gg.cartograph.plugin.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;

public class CartographBungeePlugin extends Plugin
{

    @Override
    public void onEnable()
    {
        getLogger().info("Cartograph enabled (BungeeCord)");
    }

    @Override
    public void onDisable()
    {
        getLogger().info("Cartograph disabled (BungeeCord)");
    }
}
