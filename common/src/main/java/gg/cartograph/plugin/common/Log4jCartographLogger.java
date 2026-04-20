package gg.cartograph.plugin.common;

import org.apache.logging.log4j.Logger;

/**
 * {@link CartographLogger} implementation backed by Log4j 2.
 *
 * <p>Used by the NeoForge platform.</p>
 */
public class Log4jCartographLogger implements CartographLogger
{

    private final Logger logger;

    public Log4jCartographLogger(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void debug(String message)
    {
        logger.debug(message);
    }

    @Override
    public void info(String message)
    {
        logger.info(message);
    }

    @Override
    public void warn(String message)
    {
        logger.warn(message);
    }

    @Override
    public void error(String message)
    {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable cause)
    {
        logger.error(message, cause);
    }
}
