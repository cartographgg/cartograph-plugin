package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.config.BufferConfig;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.config.TelemetryConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Defines the NeoForge configuration spec and loads values into a {@link CartographConfig}.
 *
 * <p>All config keys are declared in the static initialiser using the
 * {@link ModConfigSpec.Builder} API, which generates the TOML file that NeoForge
 * manages. The {@link #load()} method reads the current spec values and maps them
 * into the platform-agnostic config model.</p>
 *
 * <p>This class only reads from the spec — it never writes config back, makes network
 * calls, or performs any side effects.</p>
 */
public class NeoForgeConfigLoader
{

    /** The built config spec, registered with NeoForge in {@link CartographNeoForgeMod}. */
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<String> API_KEY;

    private static final ModConfigSpec.ConfigValue<String> API_ENDPOINT;

    private static final ModConfigSpec.BooleanValue FLAG_REPORT_PLUGINS;

    private static final ModConfigSpec.IntValue BUFFER_SIZE_THRESHOLD;

    private static final ModConfigSpec.IntValue BUFFER_TIME_THRESHOLD;

    private static final ModConfigSpec.IntValue BUFFER_MAX_RETRIES;

    private static final ModConfigSpec.BooleanValue HEARTBEAT_ENABLED;

    private static final ModConfigSpec.IntValue HEARTBEAT_INTERVAL;


    static {
        var builder = new ModConfigSpec.Builder();

        API_KEY = builder
                .comment("API authentication key for cartograph.gg")
                .define("api-key", "");

        API_ENDPOINT = builder
                .comment("API endpoint URL")
                .define("api-endpoint", "https://api.cartograph.gg");

        builder.comment("Feature flags").push("flags");

        FLAG_REPORT_PLUGINS = builder
                .comment("Whether to report installed mods to the API")
                .define("report-plugins", false);

        builder.pop();

        builder.comment("Global telemetry event buffer").push("buffer");

        BUFFER_SIZE_THRESHOLD = builder
                .comment("Flush buffer when it reaches this many events")
                .defineInRange("size-threshold", 50, 1, Integer.MAX_VALUE);

        BUFFER_TIME_THRESHOLD = builder
                .comment("Flush buffer after this many seconds")
                .defineInRange("time-threshold", 60, 1, Integer.MAX_VALUE);

        BUFFER_MAX_RETRIES = builder
                .comment("Maximum retry attempts on failed sends")
                .defineInRange("max-retries", 3, 0, Integer.MAX_VALUE);

        builder.pop();

        builder.comment("Telemetry type configuration").push("telemetry");

        builder.push("heartbeat");
        HEARTBEAT_ENABLED  = builder.define("enabled", true);
        HEARTBEAT_INTERVAL = builder
                .comment("Recording interval in seconds")
                .defineInRange("interval", 60, 1, Integer.MAX_VALUE);
        builder.pop();


        builder.pop();

        SPEC = builder.build();
    }

    /**
     * Reads the current values from the NeoForge config spec and returns a populated
     * {@link CartographConfig}. Should only be called after NeoForge has loaded the
     * TOML config file (i.e. during or after the server start event).
     *
     * @return a fully populated configuration built from the spec values
     */
    public static CartographConfig load()
    {
        var config = CartographConfig.defaults();

        config.setApiKey(API_KEY.get());
        config.setApiEndpoint(API_ENDPOINT.get());

        config.getFlags().put("report-plugins", FLAG_REPORT_PLUGINS.get());

        var buffer = new BufferConfig();
        buffer.setSizeThreshold(BUFFER_SIZE_THRESHOLD.get());
        buffer.setTimeThreshold(BUFFER_TIME_THRESHOLD.get());
        buffer.setMaxRetries(BUFFER_MAX_RETRIES.get());
        config.setBuffer(buffer);

        var heartbeat = new TelemetryConfig();
        heartbeat.setEnabled(HEARTBEAT_ENABLED.get());
        heartbeat.setInterval(HEARTBEAT_INTERVAL.get());
        config.getTelemetry().put("heartbeat", heartbeat);


        return config;
    }
}
