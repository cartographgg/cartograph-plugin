package gg.cartograph.plugin.common.config;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DiskBufferConfigTest
{
    @Test void defaults() {
        var d = new DiskBufferConfig();
        assertEquals(8, d.getMaxSizeMb());
        assertEquals(24, d.getMaxAgeHours());
        assertEquals(8L * 1024 * 1024, d.maxSizeBytes());
        assertEquals(Duration.ofHours(24), d.maxAge());
    }
    @Test void clampsToMinimumOne() {
        var d = new DiskBufferConfig();
        d.setMaxSizeMb(0);
        d.setMaxAgeHours(-5);
        assertEquals(1, d.getMaxSizeMb());
        assertEquals(1, d.getMaxAgeHours());
    }
}
