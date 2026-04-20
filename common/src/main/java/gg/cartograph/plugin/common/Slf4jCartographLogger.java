package gg.cartograph.plugin.common;

import org.slf4j.Logger;

/**
 * {@link CartographLogger} implementation backed by SLF4J.
 *
 * <p>Used by the Velocity platform.</p>
 */
public class Slf4jCartographLogger implements CartographLogger
{

    private final Logger logger;

    public Slf4jCartographLogger(Logger logger)
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
