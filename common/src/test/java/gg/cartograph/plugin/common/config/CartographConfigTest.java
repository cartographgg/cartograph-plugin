package gg.cartograph.plugin.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartographConfigTest
{

    @Test
    void defaultsFactoryReturnsPopulatedConfig()
    {
        var config = CartographConfig.defaults();

        assertEquals("", config.getApiKey());
        assertEquals("https://api.cartograph.gg", config.getApiEndpoint());
        assertNotNull(config.getFlags());
        assertNotNull(config.getBuffer());
        assertNotNull(config.getTelemetry());
    }

    @Test
    void defaultsHasInitialFlags()
    {
        var config = CartographConfig.defaults();

        assertTrue(config.getFlags().containsKey("report-plugins"));
        assertFalse(config.getFlags().get("report-plugins"));
    }

    @Test
    void defaultsBufferHasCorrectValues()
    {
        var config = CartographConfig.defaults();

        assertEquals(50, config.getBuffer().getSizeThreshold());
        assertEquals(60, config.getBuffer().getTimeThreshold());
        assertEquals(3, config.getBuffer().getMaxRetries());
    }

    @Test
    void defaultsHasInitialTelemetryTypes()
    {
        var config = CartographConfig.defaults();

        assertTrue(config.getTelemetry().containsKey("heartbeat"));
        assertTrue(config.getTelemetry().containsKey("tps_sample"));
        assertTrue(config.getTelemetry().containsKey("latency"));
    }

    @Test
    void defaultsTelemetryIntervalsAreCorrect()
    {
        var config = CartographConfig.defaults();

        assertEquals(60, config.getTelemetry().get("heartbeat").getInterval());
        assertEquals(20, config.getTelemetry().get("tps_sample").getInterval());
        assertEquals(30, config.getTelemetry().get("latency").getInterval());

        assertTrue(config.getTelemetry().get("heartbeat").isEnabled());
        assertTrue(config.getTelemetry().get("tps_sample").isEnabled());
        assertTrue(config.getTelemetry().get("latency").isEnabled());
    }

    @Test
    void settersOverrideDefaults()
    {
        var config = CartographConfig.defaults();
        config.setApiKey("my-key");
        config.setApiEndpoint("https://custom.endpoint");

        assertEquals("my-key", config.getApiKey());
        assertEquals("https://custom.endpoint", config.getApiEndpoint());
    }

    @Test
    void flagsMapIsMutable()
    {
        var config = CartographConfig.defaults();
        config.getFlags().put("custom-flag", true);

        assertEquals(2, config.getFlags().size());
        assertTrue(config.getFlags().get("custom-flag"));
    }

    @Test
    void telemetryMapIsMutable()
    {
        var config = CartographConfig.defaults();
        var custom = new TelemetryConfig();
        custom.setEnabled(false);
        custom.setInterval(10);
        config.getTelemetry().put("custom-type", custom);

        assertEquals(4, config.getTelemetry().size());
        assertFalse(config.getTelemetry().get("custom-type").isEnabled());
        assertEquals(10, config.getTelemetry().get("custom-type").getInterval());
    }
}
