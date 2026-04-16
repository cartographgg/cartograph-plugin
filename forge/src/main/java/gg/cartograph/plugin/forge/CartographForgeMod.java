package gg.cartograph.plugin.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("cartograph")
public class CartographForgeMod
{

    private static final Logger LOGGER = LogManager.getLogger();

    public CartographForgeMod()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Cartograph enabled (Forge)");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        LOGGER.info("Cartograph disabled (Forge)");
    }
}
