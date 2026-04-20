package gg.cartograph.plugin.common;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link CartographLogger} implementation backed by {@code java.util.logging}.
 *
 * <p>Used by Bukkit and BungeeCord platforms.</p>
 */
public class JulCartographLogger implements CartographLogger
{

    private final Logger logger;

    public JulCartographLogger(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void debug(String message)
    {
        logger.fine(message);
    }

    @Override
    public void info(String message)
    {
        logger.info(message);
    }

    @Override
    public void warn(String message)
    {
        logger.warning(message);
    }

    @Override
    public void error(String message)
    {
        logger.severe(message);
    }

    @Override
    public void error(String message, Throwable cause)
    {
        logger.log(Level.SEVERE, message, cause);
    }
}
