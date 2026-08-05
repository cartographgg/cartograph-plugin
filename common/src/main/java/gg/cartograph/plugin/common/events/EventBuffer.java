package gg.cartograph.plugin.common.events;

import gg.cartograph.plugin.common.config.BufferConfig;
import gg.cartograph.plugin.common.events.spool.PreparedBatch;
import gg.cartograph.plugin.common.events.spool.Spool;
import gg.cartograph.plugin.common.events.spool.Spooled;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Batches events and flushes them on a single daemon thread. A failed batch is
 * persisted via the {@link Spool} and retried under {@link Backoff} until it lands
 * or is evicted — there is no retry ceiling. All sends run on the flush thread.
 */
public class EventBuffer
{
    private final BufferConfig config;
    private final Function<List<TelemetryEvent>, PreparedBatch> prepare;
    private final Function<byte[], SendResult> sender;
    private final Spool spool;
    private final Backoff backoff;
    private final CartographLogger logger;
    private final ScheduledExecutorService scheduler;

    private List<TelemetryEvent> events = new ArrayList<>();
    private ScheduledFuture<?> scheduledFlush;
    private boolean running = false;

    public EventBuffer(BufferConfig config,
                       Function<List<TelemetryEvent>, PreparedBatch> prepare,
                       Function<byte[], SendResult> sender,
                       Spool spool,
                       Backoff backoff,
                       CartographLogger logger)
    {
        this.config    = config;
        this.prepare   = prepare;
        this.sender    = sender;
        this.spool     = spool;
        this.backoff   = backoff;
        this.logger    = logger;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "cartograph-buffer-flush");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start()
    {
        synchronized (this) { running = true; }
        logger.info("Event buffer started (size threshold: " + config.getSizeThreshold()
                + ", time threshold: " + config.getTimeThreshold() + "s"
                + ", failure-mode: " + config.getFailureMode().name().toLowerCase() + ")");
        scheduleFlush();
    }

    public void add(TelemetryEvent event)
    {
        boolean triggerFlush = false;
        synchronized (this) {
            if (!running) {
                logger.warn("Event added after shutdown, ignoring");
                return;
            }
            events.add(event);
            if (events.size() >= config.getSizeThreshold()) {
                triggerFlush = true;
            }
        }
        if (triggerFlush) {
            try {
                scheduler.execute(this::flush); // off the producer/tick thread
            } catch (java.util.concurrent.RejectedExecutionException ignored) {
                /* shutting down; the buffered events are handled by the final flush or dropped best-effort */
            }
        }
    }

    /** Runs only on the scheduler thread (or on the caller during shutdown's awaited submit). */
    public void flush()
    {
        try {
            List<TelemetryEvent> batch;
            synchronized (this) {
                if (events.isEmpty() && spool.size() == 0) {
                    return;
                }
                batch  = events;
                events = new ArrayList<>();
            }

            PreparedBatch live = batch.isEmpty() ? null : prepare.apply(batch);

            if (backoff.blocked()) {
                spill(live);
                return;
            }

            for (Spooled s : spool.listOldestFirst()) {
                byte[] payload = spool.load(s);
                SendResult result = payload == null ? SendResult.discard() : sender.apply(payload);
                if (result.isOk()) {
                    spool.delete(s);
                    backoff.onSuccess();
                } else if (result.isDiscard()) {
                    spool.delete(s);
                } else { // RETRY
                    armBackoff(result);
                    spill(live); // the live batch was never attempted this cycle
                    return;
                }
            }

            if (live != null) {
                SendResult result = sender.apply(live.payload());
                if (result.isOk()) {
                    backoff.onSuccess();
                } else if (result.isRetry()) {
                    armBackoff(result);
                    spill(live);
                } // DISCARD → drop
            }
        } finally {
            resetTimer();
        }
    }

    public void shutdown()
    {
        logger.info("Event buffer shutting down, flushing remaining events");
        synchronized (this) { running = false; }
        if (scheduledFlush != null) {
            scheduledFlush.cancel(false);
        }
        try {
            scheduler.submit(this::flush).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Final flush did not complete cleanly — remaining events left in the spool");
        }
        scheduler.shutdown();
    }

    private void spill(PreparedBatch live)
    {
        if (live == null) {
            return;
        }
        spool.store(live);
        spool.evict(config.getDisk().maxSizeBytes(), config.getDisk().maxAge());
    }

    private void armBackoff(SendResult result)
    {
        Duration delay = backoff.onFailure(Optional.ofNullable(result.retryAfter()));
        logger.warn("Send failed — backing off " + delay.toSeconds() + "s (" + spool.size() + " batches queued)");
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
}
