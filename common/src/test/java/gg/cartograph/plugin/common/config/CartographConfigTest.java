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
        assertTrue(config.getFlags().get("report-plugins"));
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
        assertEquals(1, config.getTelemetry().size());
    }

    @Test
    void defaultsTelemetryIntervalsAreCorrect()
    {
        var config = CartographConfig.defaults();

        assertEquals(60, config.getTelemetry().get("heartbeat").getInterval());
        assertTrue(config.getTelemetry().get("heartbeat").isEnabled());
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

        assertEquals(2, config.getTelemetry().size());
        assertFalse(config.getTelemetry().get("custom-type").isEnabled());
        assertEquals(10, config.getTelemetry().get("custom-type").getInterval());
    }
}
