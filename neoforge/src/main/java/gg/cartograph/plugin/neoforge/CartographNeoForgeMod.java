package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.Cartograph;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.logging.Log4jCartographLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * NeoForge mod entry point for the Cartograph plugin.
 *
 * <p>Registers the config spec with NeoForge's configuration system at construction
 * time, then loads the resolved values into a {@link CartographConfig} when the server
 * starts and constructs the {@link Cartograph} runtime.</p>
 *
 * @see NeoForgeConfigLoader
 */
@Mod("cartograph")
public class CartographNeoForgeMod
{

    private static final Logger LOGGER = LogManager.getLogger();

    private CartographConfig cartographConfig;

    private Cartograph cartograph;

    public CartographNeoForgeMod(IEventBus modBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigLoader.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        cartographConfig = NeoForgeConfigLoader.load();
        cartograph       = new Cartograph(cartographConfig, new Log4jCartographLogger(LOGGER));
        cartograph.start();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        if (cartograph != null) {
            cartograph.stop();
        }
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }

    public Cartograph getCartograph()
    {
        return cartograph;
    }
}
