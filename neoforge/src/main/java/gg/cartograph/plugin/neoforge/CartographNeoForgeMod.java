package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.config.CartographConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("cartograph")
public class CartographNeoForgeMod
{

    private static final Logger LOGGER = LogManager.getLogger();

    private CartographConfig cartographConfig;

    public CartographNeoForgeMod()
    {
        NeoForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NeoForgeConfigLoader.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        cartographConfig = NeoForgeConfigLoader.load();
        LOGGER.info("Cartograph enabled (NeoForge)");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        LOGGER.info("Cartograph disabled (NeoForge)");
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }
}
