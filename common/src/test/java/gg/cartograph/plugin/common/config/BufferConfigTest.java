package gg.cartograph.plugin.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BufferConfigTest
{

    @Test
    void defaultValues()
    {
        var config = new BufferConfig();

        assertEquals(50, config.getSizeThreshold());
        assertEquals(60, config.getTimeThreshold());
        assertEquals(3, config.getMaxRetries());
    }

    @Test
    void settersOverrideDefaults()
    {
        var config = new BufferConfig();
        config.setSizeThreshold(100);
        config.setTimeThreshold(120);
        config.setMaxRetries(5);

        assertEquals(100, config.getSizeThreshold());
        assertEquals(120, config.getTimeThreshold());
        assertEquals(5, config.getMaxRetries());
    }
}
