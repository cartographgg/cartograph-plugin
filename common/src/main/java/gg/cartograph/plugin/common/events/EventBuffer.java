package gg.cartograph.plugin.common.events;

import gg.cartograph.plugin.common.config.BufferConfig;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Thread-safe event buffer that batches {@link TelemetryEvent} instances and flushes
 * them to a consumer when a size threshold, time threshold, or manual flush is triggered.
 *
 * <p>Thread safety is achieved via synchronized list-swap — the lock is held only for
 * the instant it takes to swap the internal list reference, so producers are never
 * blocked during consumer execution.</p>
 *
 * <p>Failed flushes are retried on the next flush cycle, up to
 * {@link BufferConfig#getMaxRetries()} attempts before being discarded.</p>
 */
public class EventBuffer
{

    private final BufferConfig config;

    private final Consumer<List<TelemetryEvent>> consumer;

    private final CartographLogger logger;

    private final ScheduledExecutorService scheduler;

    private List<TelemetryEvent> events = new ArrayList<>();

    private List<RetryBatch> retryQueue = new ArrayList<>();

    private ScheduledFuture<?> scheduledFlush;

    private boolean running = false;

    public EventBuffer(BufferConfig config, Consumer<List<TelemetryEvent>> consumer, CartographLogger logger)
    {
        this.config    = config;
        this.consumer  = consumer;
        this.logger    = logger;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "cartograph-buffer-flush");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Starts the time-based flush scheduling.
     */
    public void start()
    {
        synchronized (this) {
            running = true;
        }
        logger.info("Event buffer started (size threshold: " + config.getSizeThreshold()
                + ", time threshold: " + config.getTimeThreshold() + "s"
                + ", max retries: " + config.getMaxRetries() + ")");
        scheduleFlush();
    }

    /**
     * Adds an event to the buffer. Triggers a size-based flush if the threshold is met.
     *
     * @param event the telemetry event to buffer
     */
    public void add(TelemetryEvent event)
    {
        boolean shouldFlush = false;

        synchronized (this) {
            if (!running) {
                logger.warn("Event added after shutdown, ignoring");
                return;
            }

            events.add(event);
            logger.debug("Added event of type: " + EventTypes.nameOf(event.type()));

            if (events.size() >= config.getSizeThreshold()) {
                logger.debug("Size threshold reached (" + config.getSizeThreshold() + "), triggering flush");
                shouldFlush = true;
            }
        }

        if (shouldFlush) {
            flush();
        }
    }

    /**
     * Manually flushes all buffered events to the consumer.
     */
    public void flush()
    {
        List<TelemetryEvent> batch;
        List<RetryBatch>     retries;

        synchronized (this) {
            if (events.isEmpty() && retryQueue.isEmpty()) {
                return;
            }
            batch      = events;
            events     = new ArrayList<>();
            retries    = retryQueue;
            retryQueue = new ArrayList<>();
        }

        logger.debug("Flushing " + batch.size() + " events (" + retries.size() + " retries pending)");

        // Deliver retries first (older data before newer)
        for (RetryBatch retry : retries) {
            deliver(retry.events(), retry.attempt());
        }

        // Deliver the current batch
        if (!batch.isEmpty()) {
            deliver(batch, 0);
        }

        resetTimer();
    }

    /**
     * Performs a final flush and shuts down the scheduler.
     */
    public void shutdown()
    {
        logger.info("Event buffer shutting down, flushing remaining events");
        synchronized (this) {
            running = false;
        }

        if (scheduledFlush != null) {
            scheduledFlush.cancel(false);
        }

        flush();
        scheduler.shutdown();
    }

    private void deliver(List<TelemetryEvent> batch, int attempt)
    {
        try {
            consumer.accept(batch);
        } catch (Exception e) {
            int nextAttempt = attempt + 1;
            if (nextAttempt < config.getMaxRetries()) {
                logger.warn("Flush failed (attempt " + nextAttempt + "/" + config.getMaxRetries() + "), will retry on next cycle");
                synchronized (this) {
                    retryQueue.add(new RetryBatch(batch, nextAttempt));
                }
            } else {
                logger.error("Batch of " + batch.size() + " events discarded after " + config.getMaxRetries() + " failed attempts", e);
            }
        }
    }

    private void scheduleFlush()
    {
        scheduledFlush = scheduler.schedule(this::flush, config.getTimeThreshold(), TimeUnit.SECONDS);
    }

    private void resetTimer()
    {
        if (scheduledFlush != null) {
            scheduledFlush.cancel(false);
        }
        synchronized (this) {
            if (running) {
                scheduleFlush();
            }
        }
    }

    private record RetryBatch(List<TelemetryEvent> events, int attempt)
    {
    }
}
