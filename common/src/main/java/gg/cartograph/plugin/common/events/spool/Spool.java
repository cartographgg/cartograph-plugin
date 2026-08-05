package gg.cartograph.plugin.common.events.spool;

import java.time.Duration;
import java.util.List;

/** Bounded store of undelivered batches. Accessed only from the flush thread. */
public interface Spool
{
    void store(PreparedBatch batch);
    List<Spooled> listOldestFirst();
    byte[] load(Spooled spooled);
    void delete(Spooled spooled);
    void evict(long maxBytes, Duration maxAge);
    int size();
}
