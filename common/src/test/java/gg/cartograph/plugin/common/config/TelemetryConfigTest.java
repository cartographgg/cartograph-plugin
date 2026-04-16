package gg.cartograph.plugin.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryConfigTest
{

    @Test
    void defaultValues()
    {
        var config = new TelemetryConfig();

        assertTrue(config.isEnabled());
        assertEquals(60, config.getInterval());
    }

    @Test
    void settersOverrideDefaults()
    {
        var config = new TelemetryConfig();
        config.setEnabled(false);
        config.setInterval(30);

        assertFalse(config.isEnabled());
        assertEquals(30, config.getInterval());
    }
}
