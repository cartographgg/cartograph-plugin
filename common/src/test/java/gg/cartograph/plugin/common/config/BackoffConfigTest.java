package gg.cartograph.plugin.common.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BackoffConfigTest
{
    @Test void defaults() {
        var b = new BackoffConfig();
        assertEquals(900, b.getMaxSeconds());
        assertEquals(3600, b.getRetryAfterCapSeconds());
    }
}
