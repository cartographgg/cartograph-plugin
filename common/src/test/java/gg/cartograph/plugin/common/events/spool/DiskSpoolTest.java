package gg.cartograph.plugin.common.events.spool;

import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DiskSpoolTest
{
    private final CartographLogger logger = mock(CartographLogger.class);

    private PreparedBatch batch(long created, byte[] bytes) { return new PreparedBatch(bytes, created); }

    private long jsonFileCount(Path dir) throws Exception {
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(p -> p.getFileName().toString().endsWith(".json")).count();
        }
    }

    @Test void storeWritesOneFilePerBatchAndListsOldestFirst(@TempDir Path dir) throws Exception {
        var spool = new DiskSpool(dir, logger);
        spool.store(batch(100, "a".getBytes()));
        spool.store(batch(200, "bb".getBytes()));
        assertEquals(2, jsonFileCount(dir));
        var list = spool.listOldestFirst();
        assertEquals(100, list.get(0).createdAtEpochMs());
        assertEquals(200, list.get(1).createdAtEpochMs());
        assertArrayEquals("a".getBytes(), spool.load(list.get(0)));
    }

    @Test void deleteRemovesFile(@TempDir Path dir) throws Exception {
        var spool = new DiskSpool(dir, logger);
        spool.store(batch(100, "x".getBytes()));
        spool.delete(spool.listOldestFirst().get(0));
        assertEquals(0, spool.size());
        assertEquals(0, jsonFileCount(dir));
    }

    @Test void survivesRestartByRescanning(@TempDir Path dir) throws Exception {
        var first = new DiskSpool(dir, logger);
        first.store(batch(100, "persisted".getBytes()));
        var reopened = new DiskSpool(dir, logger);
        assertEquals(1, reopened.size());
        assertArrayEquals("persisted".getBytes(), reopened.load(reopened.listOldestFirst().get(0)));
    }

    @Test void sweepsOrphanTempFilesOnScan(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("123-1.json.tmp"), "half-written");
        new DiskSpool(dir, logger);
        assertFalse(Files.exists(dir.resolve("123-1.json.tmp")));
    }

    @Test void evictsBySizeDropOldest(@TempDir Path dir) throws Exception {
        var spool = new DiskSpool(dir, logger);
        long t1 = System.currentTimeMillis() - 1000;
        long t2 = System.currentTimeMillis();
        spool.store(batch(t1, new byte[6]));
        spool.store(batch(t2, new byte[6]));
        spool.evict(8, Duration.ofHours(24));  // both recent (age no-op) → only size evicts oldest
        assertEquals(1, spool.size());
        assertEquals(t2, spool.listOldestFirst().get(0).createdAtEpochMs());
    }

    @Test void evictsByAge(@TempDir Path dir) throws Exception {
        var spool = new DiskSpool(dir, logger);
        long old = System.currentTimeMillis() - Duration.ofHours(48).toMillis();
        spool.store(batch(old, new byte[4]));
        spool.store(batch(System.currentTimeMillis(), new byte[4]));
        spool.evict(1024, Duration.ofHours(24));
        assertEquals(1, spool.size());
    }
}
