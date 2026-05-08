package gg.cartograph.plugin.common;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.events.EventBuffer;
import gg.cartograph.plugin.common.events.EventTypes;
import gg.cartograph.plugin.common.events.TelemetryClient;
import gg.cartograph.plugin.common.events.telemetry.HeartbeatTelemetryEvent;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Central coordinator for the Cartograph telemetry runtime.
 *
 * <p>Each platform plugin constructs a single instance, passing in the loaded
 * configuration, a platform-appropriate {@link CartographLogger}, and a supplier
 * that builds heartbeat events from platform-specific metrics.</p>
 */
public class Cartograph
{

    private final CartographConfig config;

    private final CartographLogger logger;

    private final Supplier<HeartbeatTelemetryEvent> heartbeatSupplier;

    private EventBuffer buffer;

    private ScheduledExecutorService heartbeatScheduler;

    private SessionTracker sessionTracker;

    private TickSampler tickSampler;

    private TelemetryClient telemetryClient;

    private long startTime;

    public Cartograph(CartographConfig config, CartographLogger logger, Supplier<HeartbeatTelemetryEvent> heartbeatSupplier)
    {
        this.config            = config;
        this.logger            = logger;
        this.heartbeatSupplier = heartbeatSupplier;
    }

    /**
     * Creates the event buffer and starts time-based flush scheduling.
     * Also starts the heartbeat scheduler if enabled in config.
     */
    public void start()
    {
        startTime = System.currentTimeMillis();
        sessionTracker = new SessionTracker(logger);
        telemetryClient = new TelemetryClient(config.getApiEndpoint(), config.getApiKey(), logger);
        buffer = new EventBuffer(config.getBuffer(), telemetryClient::send, logger);
        buffer.start();
        startHeartbeat();
        logger.info("Cartograph started");
    }

    private void startHeartbeat()
    {
        var heartbeatConfig = config.getTelemetry().get("heartbeat");
        if (heartbeatConfig == null || !heartbeatConfig.isEnabled()) {
            logger.info("Heartbeat disabled");
            return;
        }

        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "cartograph-heartbeat");
            thread.setDaemon(true);
            return thread;
        });

        heartbeatScheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        record(heartbeatSupplier.get());
                        logger.debug("Heartbeat collected");
                    } catch (Exception e) {
                        logger.error("Failed to collect heartbeat", e);
                    }
                }, heartbeatConfig.getInterval(), heartbeatConfig.getInterval(), TimeUnit.SECONDS
        );
        logger.info("Heartbeat scheduled every " + heartbeatConfig.getInterval() + "s");
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
        logger.debug("Recording event of type: " + EventTypes.nameOf(event.type()));
        buffer.add(event);
    }

    public CartographLogger getLogger()
    {
        return logger;
    }

    public SessionTracker getSessionTracker()
    {
        return sessionTracker;
    }

    public synchronized TickSampler getTickSampler()
    {
        if (tickSampler == null) {
            tickSampler = new TickSampler();
        }
        return tickSampler;
    }

    /**
     * Returns the time in milliseconds since {@link #start()} was called.
     */
    public long getUptime()
    {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Shuts down the heartbeat scheduler and event buffer.
     */
    public void stop()
    {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
            heartbeatScheduler = null;
        }
        if (buffer != null) {
            buffer.shutdown();
            buffer = null;
        }
        if (telemetryClient != null) {
            telemetryClient.close();
            telemetryClient = null;
        }
        sessionTracker = null;
        logger.info("Cartograph stopped");
    }

    public boolean shouldReportPlugins()
    {
        return config.getFlags().getOrDefault("report-plugins", false);
    }

    public boolean isProxyBackend()
    {
        return config.getFlags().getOrDefault("proxy-backend", false);
    }
}
