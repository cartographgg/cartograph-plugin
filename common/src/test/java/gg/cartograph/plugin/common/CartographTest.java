package gg.cartograph.plugin.common;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class CartographTest
{

    private CartographConfig config;

    private CartographLogger logger;

    private Cartograph cartograph;

    @BeforeEach
    void setUp()
    {
        config = CartographConfig.defaults();
        config.getBuffer().setSizeThreshold(3);
        config.getBuffer().setTimeThreshold(60);
        logger     = mock(CartographLogger.class);
        cartograph = new Cartograph(config, logger);
    }

    @Test
    void startAndStopLifecycle()
    {
        cartograph.start();
        cartograph.stop();

        verify(logger, never()).error(anyString());
    }

    @Test
    void recordDelegatesToBuffer()
    {
        cartograph.start();

        cartograph.record(event("heartbeat"));
        cartograph.record(event("heartbeat"));
        cartograph.record(event("heartbeat"));

        // Buffer should have flushed (size threshold = 3), logging at debug level
        verify(logger).debug(contains("3"));
        cartograph.stop();
    }

    @Test
    void recordBeforeStartLogsWarning()
    {
        cartograph.record(event("heartbeat"));

        verify(logger).warn(contains("started"));
    }

    @Test
    void recordAfterStopLogsWarning()
    {
        cartograph.start();
        cartograph.stop();

        cartograph.record(event("heartbeat"));

        verify(logger).warn(anyString());
    }

    @Test
    void stopFlushesRemainingEvents()
    {
        cartograph.start();
        cartograph.record(event("heartbeat"));
        cartograph.record(event("tps_sample"));
        cartograph.stop();

        // Final flush should have logged at debug level
        verify(logger).debug(contains("2"));
    }

    private TelemetryEvent event(String type)
    {
        return new TelemetryEvent()
        {
            @Override
            public String type()
            {
                return type;
            }

            @Override
            public Long timestamp()
            {
                return System.currentTimeMillis();
            }
        };
    }
}
