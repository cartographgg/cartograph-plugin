package gg.cartograph.plugin.common.events.spool;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class MemorySpoolTest
{
    private PreparedBatch batch(long created, int bytes) {
        return new PreparedBatch(new byte[bytes], created);
    }

    @Test void storesAndListsOldestFirst() {
        var s = new MemorySpool();
        s.store(batch(100, 4));
        s.store(batch(200, 4));
        var list = s.listOldestFirst();
        assertEquals(2, list.size());
        assertEquals(100, list.get(0).createdAtEpochMs());
        assertEquals(200, list.get(1).createdAtEpochMs());
    }

    @Test void loadReturnsPayloadThenDeleteRemoves() {
        var s = new MemorySpool();
        s.store(batch(100, 3));
        var only = s.listOldestFirst().get(0);
        assertEquals(3, s.load(only).length);
        s.delete(only);
        assertEquals(0, s.size());
    }

    @Test void evictsBySizeDropOldest() {
        var s = new MemorySpool();
        long now = System.currentTimeMillis();
        s.store(batch(now - 1000, 6));
        s.store(batch(now, 6));
        s.evict(8, Duration.ofHours(24));  // 12 bytes > 8 → drop oldest
        assertEquals(1, s.size());
        assertEquals(now, s.listOldestFirst().get(0).createdAtEpochMs());
    }

    @Test void evictsByAge() {
        var s = new MemorySpool();
        long old = System.currentTimeMillis() - Duration.ofHours(48).toMillis();
        s.store(batch(old, 4));
        s.store(batch(System.currentTimeMillis(), 4));
        s.evict(1024, Duration.ofHours(24));
        assertEquals(1, s.size());
    }
}
