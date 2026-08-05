package gg.cartograph.plugin.common.events.spool;

import gg.cartograph.plugin.common.logging.CartographLogger;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * failure-mode: disk — one file per undelivered batch, written atomically
 * (temp + rename). Filenames are {@code <zeroPaddedEpochMs>-<seq>.json} so lexical
 * order is oldest-first. An in-memory index (name → {path,createdAt,size}) keeps
 * eviction and totals O(log n) with no repeated directory walks. Not thread-safe;
 * used only from the flush thread. No fsync (telemetry-grade durability).
 */
public class DiskSpool implements Spool
{
    private static final String EXT     = ".json";
    private static final String TMP_EXT = ".tmp";

    private record Entry(Path path, long createdAtEpochMs, long sizeBytes) { }

    private final Path dir;
    private final CartographLogger logger;
    private final TreeMap<String, Entry> index = new TreeMap<>();
    private long totalBytes = 0;
    private long seq = 0;

    public DiskSpool(Path dir, CartographLogger logger) throws IOException
    {
        this.dir    = dir;
        this.logger = logger;
        Files.createDirectories(dir);
        scan();
    }

    private void scan() throws IOException
    {
        try (var stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String name = p.getFileName().toString();
                if (name.endsWith(TMP_EXT)) {
                    tryDelete(p);
                    continue;
                }
                if (!name.endsWith(EXT)) {
                    continue;
                }
                long size = Files.size(p);
                index.put(name, new Entry(p, parseCreatedAt(name), size));
                totalBytes += size;
            }
        }
    }

    public void store(PreparedBatch batch)
    {
        String name = fileName(batch.createdAtEpochMs());
        Path tmp = dir.resolve(name + TMP_EXT);
        Path fin = dir.resolve(name);
        try {
            Files.write(tmp, batch.payload(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(tmp, fin, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, fin, StandardCopyOption.REPLACE_EXISTING);
            }
            Entry old = index.put(name, new Entry(fin, batch.createdAtEpochMs(), batch.payload().length));
            totalBytes += batch.payload().length;
            if (old != null) {
                totalBytes -= old.sizeBytes();
            }
        } catch (IOException e) {
            logger.warn("Failed to spill telemetry batch to disk: " + e.getMessage());
            tryDelete(tmp);
        }
    }

    public List<Spooled> listOldestFirst()
    {
        var out = new ArrayList<Spooled>(index.size());
        for (Map.Entry<String, Entry> e : index.entrySet()) {
            out.add(new Spooled(e.getValue().createdAtEpochMs(), e.getValue().sizeBytes(), e.getKey()));
        }
        return out;
    }

    public byte[] load(Spooled spooled)
    {
        Entry e = index.get((String) spooled.handle());
        if (e == null) {
            return null;
        }
        try {
            return Files.readAllBytes(e.path());
        } catch (IOException ex) {
            logger.warn("Unreadable spool file " + e.path().getFileName() + " — dropping");
            removeByName((String) spooled.handle());
            return null;
        }
    }

    public void delete(Spooled spooled) { removeByName((String) spooled.handle()); }

    public void evict(long maxBytes, Duration maxAge)
    {
        long cutoff = System.currentTimeMillis() - maxAge.toMillis();
        for (Iterator<Map.Entry<String, Entry>> it = index.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Entry> e = it.next();
            if (e.getValue().createdAtEpochMs() >= cutoff) {
                break; // index is oldest-first by name≈createdAt
            }
            totalBytes -= e.getValue().sizeBytes();
            tryDelete(e.getValue().path());
            it.remove();
        }
        while (totalBytes > maxBytes && !index.isEmpty()) {
            Map.Entry<String, Entry> first = index.firstEntry();
            totalBytes -= first.getValue().sizeBytes();
            tryDelete(first.getValue().path());
            index.remove(first.getKey());
        }
    }

    public int size() { return index.size(); }

    private void removeByName(String name)
    {
        Entry e = index.remove(name);
        if (e != null) {
            totalBytes -= e.sizeBytes();
            tryDelete(e.path());
        }
    }

    private String fileName(long createdAtEpochMs)
    {
        return String.format("%019d-%09d%s", createdAtEpochMs, seq++, EXT);
    }

    private static long parseCreatedAt(String name)
    {
        try {
            return Long.parseLong(name.substring(0, name.indexOf('-')));
        } catch (RuntimeException e) {
            return 0L;
        }
    }

    private void tryDelete(Path p)
    {
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
        }
    }
}
