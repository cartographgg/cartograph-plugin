package gg.cartograph.plugin.common.events.spool;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/** failure-mode: memory — bounded in-RAM deque, drop-oldest, lost on restart. */
public class MemorySpool implements Spool
{
    private record Item(long id, byte[] payload, long createdAtEpochMs) { }

    private final Deque<Item> items = new ArrayDeque<>();
    private long totalBytes = 0;
    private long seq = 0;

    public void store(PreparedBatch batch)
    {
        items.addLast(new Item(seq++, batch.payload(), batch.createdAtEpochMs()));
        totalBytes += batch.payload().length;
    }

    public List<Spooled> listOldestFirst()
    {
        var out = new ArrayList<Spooled>(items.size());
        for (Item i : items) {
            out.add(new Spooled(i.createdAtEpochMs(), i.payload().length, i.id()));
        }
        return out;
    }

    public byte[] load(Spooled spooled)
    {
        long id = (Long) spooled.handle();
        for (Item i : items) {
            if (i.id() == id) {
                return i.payload();
            }
        }
        return null;
    }

    public void delete(Spooled spooled)
    {
        long id = (Long) spooled.handle();
        for (Iterator<Item> it = items.iterator(); it.hasNext(); ) {
            Item i = it.next();
            if (i.id() == id) {
                totalBytes -= i.payload().length;
                it.remove();
                return;
            }
        }
    }

    public void evict(long maxBytes, Duration maxAge)
    {
        long cutoff = System.currentTimeMillis() - maxAge.toMillis();
        while (!items.isEmpty() && items.peekFirst().createdAtEpochMs() < cutoff) {
            totalBytes -= items.pollFirst().payload().length;
        }
        while (totalBytes > maxBytes && !items.isEmpty()) {
            totalBytes -= items.pollFirst().payload().length;
        }
    }

    public int size() { return items.size(); }
}
