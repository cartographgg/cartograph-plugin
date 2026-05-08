package gg.cartograph.plugin.common;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.telemetry.HeartbeatTelemetryEvent;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CartographTest
{

    private CartographConfig config;

    private CartographLogger logger;

    private Supplier<HeartbeatTelemetryEvent> heartbeatSupplier;

    private Cartograph cartograph;

    @BeforeEach
    void setUp()
    {
        config = CartographConfig.defaults();
        config.getBuffer().setSizeThreshold(3);
        config.getBuffer().setTimeThreshold(60);
        logger            = mock(CartographLogger.class);
        heartbeatSupplier = () -> new HeartbeatTelemetryEvent(
                System.currentTimeMillis(), null, null, null, 0, 0L, 0L, 0.0, 0.0, 0, null, null, null
        );
        cartograph        = new Cartograph(config, logger, heartbeatSupplier);
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

        verify(logger).warn("Telemetry not sent \u2014 API key is not configured");
        cartograph.stop();
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

    @Test
    void recordBeforeStartLogsWarning()
    {
        cartograph.record(event("heartbeat"));

        verify(logger).warn("Cannot record event \u2014 Cartograph not started");
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

        verify(logger).warn("Telemetry not sent \u2014 API key is not configured");
    }

    @Test
    void heartbeatNotScheduledWhenDisabled()
    {
        config.getTelemetry().get("heartbeat").setEnabled(false);
        cartograph = new Cartograph(config, logger, heartbeatSupplier);
        cartograph.start();

        verify(logger).info("Heartbeat disabled");
        verify(logger, never()).error(anyString(), any(Throwable.class));
        cartograph.stop();
    }

    @Test
    void heartbeatScheduledLogsInterval()
    {
        cartograph.start();

        verify(logger).info("Heartbeat scheduled every 60s");
        cartograph.stop();
    }

    @Test
    void getLoggerReturnsInjectedLogger()
    {
        assertEquals(logger, cartograph.getLogger());
    }

    @Test
    void getSessionTrackerReturnsNullBeforeStart()
    {
        assertNull(cartograph.getSessionTracker());
    }

    @Test
    void getSessionTrackerReturnsInstanceAfterStart()
    {
        cartograph.start();
        assertNotNull(cartograph.getSessionTracker());
        cartograph.stop();
    }

    @Test
    void getSessionTrackerReturnsNullAfterStop()
    {
        cartograph.start();
        cartograph.stop();
        assertNull(cartograph.getSessionTracker());
    }

    @Test
    void getTickSamplerCreatesLazily()
    {
        var sampler = cartograph.getTickSampler();
        assertNotNull(sampler);
        assertSame(sampler, cartograph.getTickSampler());
    }
}
