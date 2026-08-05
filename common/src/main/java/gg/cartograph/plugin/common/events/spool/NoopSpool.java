package gg.cartograph.plugin.common.events.spool;

import java.time.Duration;
import java.util.List;

/** failure-mode: none — undelivered batches are dropped. */
public class NoopSpool implements Spool
{
    public void store(PreparedBatch batch) { }
    public List<Spooled> listOldestFirst() { return List.of(); }
    public byte[] load(Spooled spooled) { return null; }
    public void delete(Spooled spooled) { }
    public void evict(long maxBytes, Duration maxAge) { }
    public int size() { return 0; }
}
