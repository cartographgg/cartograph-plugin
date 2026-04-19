package gg.cartograph.plugin.forge;

import gg.cartograph.plugin.common.config.CartographConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forge mod entry point for the Cartograph plugin.
 *
 * <p>Registers the config spec with Forge's configuration system at construction time,
 * then loads the resolved values into a {@link CartographConfig} when the server starts.
 * The config file is managed by Forge and stored in the server's config directory as a
 * TOML file.</p>
 *
 * <p>No telemetry or network activity occurs until the server start event fires and
 * configuration has been loaded successfully.</p>
 *
 * @see ForgeConfigLoader
 */
@Mod("cartograph")
public class CartographForgeMod
{

    private static final Logger LOGGER = LogManager.getLogger();

    private CartographConfig cartographConfig;

    public CartographForgeMod()
    {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeConfigLoader.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        cartographConfig = ForgeConfigLoader.load();
        LOGGER.info("Cartograph enabled (Forge)");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event)
    {
        LOGGER.info("Cartograph disabled (Forge)");
    }

    public CartographConfig getCartographConfig()
    {
        return cartographConfig;
    }
}
