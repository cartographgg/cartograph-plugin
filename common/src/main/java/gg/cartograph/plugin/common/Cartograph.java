package gg.cartograph.plugin.common;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.EventBuffer;
import gg.cartograph.plugin.common.events.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;

import java.util.List;

/**
 * Central coordinator for the Cartograph telemetry runtime.
 *
 * <p>Each platform plugin constructs a single instance, passing in the loaded
 * configuration and a platform-appropriate {@link CartographLogger}. This class
 * owns the {@link EventBuffer} lifecycle and provides {@link #record(TelemetryEvent)}
 * as the entry point for all telemetry events.</p>
 *
 * <p>The flush consumer currently logs at debug level. It will be replaced by an
 * HTTP API client in a future iteration.</p>
 */
public class Cartograph
{

    private final CartographConfig config;

    private final CartographLogger logger;

    private EventBuffer buffer;

    public Cartograph(CartographConfig config, CartographLogger logger)
    {
        this.config = config;
        this.logger = logger;
    }

    /**
     * Creates the event buffer and starts time-based flush scheduling.
     */
    public void start()
    {
        buffer = new EventBuffer(config.getBuffer(), this::flushEvents, logger);
        buffer.start();
        logger.info("Cartograph started");
    }

    private void flushEvents(List<TelemetryEvent> events)
    {
        logger.debug("Flushing batch of " + events.size() + " events");
    }

    /**
     * Shuts down the event buffer, performing a final flush.
     */
    public void stop()
    {
        if (buffer != null) {
            buffer.shutdown();
            buffer = null;
        }
        logger.info("Cartograph stopped");
    }

    /**
     * Records a telemetry event into the buffer.
     *
     * @param event the telemetry event to record
     */
    public void record(TelemetryEvent event)
    {
        if (buffer == null) {
            logger.warn("Cannot record event — Cartograph not started");
            return;
        }
        buffer.add(event);
    }
}
