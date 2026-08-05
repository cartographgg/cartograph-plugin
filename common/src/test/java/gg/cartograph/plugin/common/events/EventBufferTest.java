package gg.cartograph.plugin.common.events;

import gg.cartograph.plugin.common.config.BufferConfig;
import gg.cartograph.plugin.common.events.spool.MemorySpool;
import gg.cartograph.plugin.common.events.spool.PreparedBatch;
import gg.cartograph.plugin.common.events.spool.Spool;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventBufferTest
{
    private BufferConfig config;
    private CartographLogger logger;
    private Spool spool;
    private Backoff backoff;
    private final CopyOnWriteArrayList<byte[]> sent = new CopyOnWriteArrayList<>();

    // A deterministic prepare: payload encodes the batch size so tests can assert.
    // Stamped with the real clock (not a fixed epoch) so spool.evict()'s age-based
    // cutoff (relative to "now") doesn't immediately reap what was just spilled.
    private final Function<List<TelemetryEvent>, PreparedBatch> prepare =
            events -> new PreparedBatch(("n=" + events.size()).getBytes(), System.currentTimeMillis());

    @BeforeEach void setUp() {
        config = new BufferConfig();
        config.setSizeThreshold(3);
        config.setTimeThreshold(1);
        logger  = mock(CartographLogger.class);
        spool   = new MemorySpool();
        backoff = new Backoff(config.getBackoff());
        sent.clear();
    }

    private TelemetryEvent event() {
        return new TelemetryEvent() {
            public String type() { return "heartbeat"; }
            public Long timestamp() { return 1L; }
        };
    }

    private EventBuffer buffer(Function<byte[], SendResult> sender) {
        return new EventBuffer(config, prepare, sender, spool, backoff, logger);
    }

    // Poll helper avoids depending on real timing.
    private void awaitSent(int count) {
        long deadline = System.nanoTime() + Duration.ofSeconds(3).toNanos();
        while (sent.size() < count && System.nanoTime() < deadline) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) { }
        }
    }

    @Test void sizeThresholdFlushesOffThreadAndSends() {
        var b = buffer(p -> { sent.add(p); return SendResult.ok(); });
        b.start();
        b.add(event()); b.add(event()); b.add(event());
        awaitSent(1);
        assertEquals(1, sent.size());
        assertArrayEquals("n=3".getBytes(), sent.get(0));
        b.shutdown();
    }

    @Test void failedSendSpillsAndReplaysOnNextFlush() {
        var attempt = new AtomicInteger();
        var b = buffer(p -> {
            sent.add(p);
            return attempt.getAndIncrement() == 0 ? SendResult.retry(Duration.ofSeconds(0)) : SendResult.ok();
        });
        b.start();
        b.add(event()); b.add(event()); b.add(event()); // first flush → RETRY(0) → spilled
        awaitSent(1);
        assertEquals(1, spoolAfter(b), "batch is spooled after failure");
        b.flush(); // backoff of 0s expired → drains spool → OK → deletes
        awaitSent(2);
        assertEquals(0, spoolAfter(b));
        b.shutdown();
    }

    private int spoolAfter(EventBuffer b) { return spool.size(); }

    @Test void fourxxDiscardsWithoutSpilling() {
        var b = buffer(p -> { sent.add(p); return SendResult.discard(); });
        b.start();
        b.add(event()); b.add(event()); b.add(event());
        awaitSent(1);
        assertEquals(0, spool.size());
        b.shutdown();
    }

    @Test void shutdownSpillsWhenSendFails() {
        var b = buffer(p -> { sent.add(p); return SendResult.retry(Duration.ofSeconds(30)); });
        b.start();
        b.add(event());
        b.shutdown();
        assertEquals(1, spool.size()); // final flush failed → spilled, not lost
    }

    @Test void startLogsFailureMode() {
        var b = buffer(p -> SendResult.ok());
        b.start();
        verify(logger).info("Event buffer started (size threshold: 3, time threshold: 1s, failure-mode: disk)");
        b.shutdown();
    }
}
