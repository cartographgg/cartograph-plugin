package gg.cartograph.plugin.common.events.spool;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class NoopSpoolTest
{
    @Test void dropsEverything() {
        var s = new NoopSpool();
        s.store(new PreparedBatch(new byte[10], 1));
        assertEquals(0, s.size());
        assertTrue(s.listOldestFirst().isEmpty());
    }
}
