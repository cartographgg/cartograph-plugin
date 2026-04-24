package gg.cartograph.plugin.common.events;

import gg.cartograph.plugin.common.config.BufferConfig;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventBufferTest
{

    private BufferConfig config;

    private List<List<TelemetryEvent>> flushedBatches;

    private CartographLogger logger;

    private EventBuffer buffer;

    @BeforeEach
    void setUp()
    {
        config = new BufferConfig();
        config.setSizeThreshold(3);
        config.setTimeThreshold(1);
        config.setMaxRetries(2);

        flushedBatches = new CopyOnWriteArrayList<>();
        logger         = mock(CartographLogger.class);
        buffer         = new EventBuffer(config, flushedBatches::add, logger);
    }

    @Test
    void addDoesNotFlushBelowThreshold()
    {
        buffer.start();
        buffer.add(event("heartbeat"));
        buffer.add(event("heartbeat"));

        assertTrue(flushedBatches.isEmpty());
        buffer.shutdown();
    }

    private TelemetryEvent event(String type)
    {
        return new TelemetryEvent()
        {
            @Override
            public String type()
            {
                return type;
            }

            @Override
            public Long timestamp()
            {
                return System.currentTimeMillis();
            }
        };
    }

    @Test
    void flushTriggeredWhenSizeThresholdReached()
    {
        buffer.start();
        buffer.add(event("heartbeat"));
        buffer.add(event("tps_sample"));
        buffer.add(event("latency"));

        assertEquals(1, flushedBatches.size());
        assertEquals(3, flushedBatches.get(0).size());
        buffer.shutdown();
    }

    @Test
    void manualFlushDrainsPendingEvents()
    {
        buffer.start();
        buffer.add(event("heartbeat"));
        buffer.add(event("tps_sample"));
        buffer.flush();

        assertEquals(1, flushedBatches.size());
        assertEquals(2, flushedBatches.get(0).size());
        buffer.shutdown();
    }

    @Test
    void manualFlushNoopsWhenEmpty()
    {
        buffer.start();
        buffer.flush();

        assertTrue(flushedBatches.isEmpty());
        buffer.shutdown();
    }

    @Test
    void timeBasedFlush() throws InterruptedException
    {
        buffer.start();
        buffer.add(event("heartbeat"));

        // Wait for time threshold (1 second) plus buffer
        Thread.sleep(1500);

        assertEquals(1, flushedBatches.size());
        assertEquals(1, flushedBatches.get(0).size());
        buffer.shutdown();
    }

    @Test
    void shutdownFlushesRemainingEvents()
    {
        buffer.start();
        buffer.add(event("heartbeat"));
        buffer.add(event("tps_sample"));
        buffer.shutdown();

        assertEquals(1, flushedBatches.size());
        assertEquals(2, flushedBatches.get(0).size());
    }

    @Test
    void addAfterShutdownIsNoOp()
    {
        buffer.start();
        buffer.shutdown();
        buffer.add(event("heartbeat"));

        assertTrue(flushedBatches.isEmpty());
        verify(logger).warn("Event added after shutdown, ignoring");
    }

    @Test
    void retriesFailedBatchOnNextFlush()
    {
        var attempts = new CopyOnWriteArrayList<List<TelemetryEvent>>();
        var failOnce = new AtomicBoolean(true);

        var failingBuffer = new EventBuffer(
                config, batch -> {
            attempts.add(new ArrayList<>(batch));
            if (failOnce.getAndSet(false)) {
                throw new RuntimeException("send failed");
            }
        }, logger
        );

        failingBuffer.start();
        failingBuffer.add(event("heartbeat"));
        failingBuffer.add(event("heartbeat"));
        failingBuffer.add(event("heartbeat"));

        // First flush failed, now trigger another to retry
        failingBuffer.add(event("tps_sample"));
        failingBuffer.add(event("tps_sample"));
        failingBuffer.add(event("tps_sample"));

        // First attempt failed, second attempt (retry) succeeded, third is the new batch
        assertEquals(3, attempts.size());
        assertEquals(3, attempts.get(0).size()); // original failed batch
        assertEquals(3, attempts.get(1).size()); // retry of failed batch
        assertEquals(3, attempts.get(2).size()); // new batch
        failingBuffer.shutdown();
    }

    @Test
    void discardsAfterMaxRetries()
    {
        var failingBuffer = new EventBuffer(
                config, batch -> {
            throw new RuntimeException("always fails");
        }, logger
        );

        failingBuffer.start();

        // First flush — fails, queued for retry (attempt 1)
        failingBuffer.add(event("heartbeat"));
        failingBuffer.add(event("heartbeat"));
        failingBuffer.add(event("heartbeat"));

        // Second flush — retry fails (attempt 2 = maxRetries), discard
        failingBuffer.add(event("tps_sample"));
        failingBuffer.add(event("tps_sample"));
        failingBuffer.add(event("tps_sample"));

        // Third flush — the first batch should be gone now, only new batch retried
        failingBuffer.add(event("latency"));
        failingBuffer.add(event("latency"));
        failingBuffer.add(event("latency"));

        verify(logger, atLeastOnce()).error(eq("Batch of 3 events discarded after 2 failed attempts"), any(Throwable.class));
        failingBuffer.shutdown();
    }

    @Test
    void threadSafetyUnderConcurrentAdds() throws InterruptedException
    {
        config.setSizeThreshold(100);
        var concurrentBuffer = new EventBuffer(config, flushedBatches::add, logger);
        concurrentBuffer.start();

        var threads         = 10;
        var eventsPerThread = 10;
        var latch           = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    concurrentBuffer.add(event("heartbeat"));
                }
                latch.countDown();
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);
        concurrentBuffer.shutdown();

        var totalFlushed = flushedBatches.stream().mapToInt(List::size).sum();
        assertEquals(100, totalFlushed);
    }

    @Test
    void startLogsBufferConfig()
    {
        buffer.start();

        verify(logger).info("Event buffer started (size threshold: 3, time threshold: 1s, max retries: 2)");
        buffer.shutdown();
    }

    @Test
    void shutdownLogsMessage()
    {
        buffer.start();
        buffer.shutdown();

        verify(logger).info("Event buffer shutting down, flushing remaining events");
    }
}
