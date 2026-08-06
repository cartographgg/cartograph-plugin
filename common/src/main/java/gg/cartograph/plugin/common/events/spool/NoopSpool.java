package gg.cartograph.plugin.common.events.spool;

import java.time.Duration;
import java.util.List;

/** failure-mode: none — undelivered batches are dropped. */
public class NoopSpool implements Spool
{
    @Override
    public void store(PreparedBatch batch) { }
    @Override
    public List<Spooled> listOldestFirst() { return List.of(); }
    @Override
    public byte[] load(Spooled spooled) { return null; }
    @Override
    public void delete(Spooled spooled) { }
    @Override
    public void evict(long maxBytes, Duration maxAge) { }
    @Override
    public int size() { return 0; }
}
