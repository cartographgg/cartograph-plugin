package gg.cartograph.plugin.forge;

import gg.cartograph.plugin.common.config.BufferConfig;
import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.config.TelemetryConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfigLoader
{

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<String>  API_KEY;
    private static final ForgeConfigSpec.ConfigValue<String>  API_ENDPOINT;

    private static final ForgeConfigSpec.BooleanValue         FLAG_REPORT_PLUGINS;

    private static final ForgeConfigSpec.IntValue             BUFFER_SIZE_THRESHOLD;
    private static final ForgeConfigSpec.IntValue             BUFFER_TIME_THRESHOLD;
    private static final ForgeConfigSpec.IntValue             BUFFER_MAX_RETRIES;

    private static final ForgeConfigSpec.BooleanValue         HEARTBEAT_ENABLED;
    private static final ForgeConfigSpec.IntValue             HEARTBEAT_INTERVAL;
    private static final ForgeConfigSpec.BooleanValue         TPS_SAMPLE_ENABLED;
    private static final ForgeConfigSpec.IntValue             TPS_SAMPLE_INTERVAL;
    private static final ForgeConfigSpec.BooleanValue         LATENCY_ENABLED;
    private static final ForgeConfigSpec.IntValue             LATENCY_INTERVAL;

    static
    {
        var builder = new ForgeConfigSpec.Builder();

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
        HEARTBEAT_ENABLED = builder.define("enabled", true);
        HEARTBEAT_INTERVAL = builder
                .comment("Recording interval in seconds")
                .defineInRange("interval", 60, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("tps_sample");
        TPS_SAMPLE_ENABLED = builder.define("enabled", true);
        TPS_SAMPLE_INTERVAL = builder
                .comment("Recording interval in seconds")
                .defineInRange("interval", 20, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("latency");
        LATENCY_ENABLED = builder.define("enabled", true);
        LATENCY_INTERVAL = builder
                .comment("Recording interval in seconds")
                .defineInRange("interval", 30, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }

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

        var tpsSample = new TelemetryConfig();
        tpsSample.setEnabled(TPS_SAMPLE_ENABLED.get());
        tpsSample.setInterval(TPS_SAMPLE_INTERVAL.get());
        config.getTelemetry().put("tps_sample", tpsSample);

        var latency = new TelemetryConfig();
        latency.setEnabled(LATENCY_ENABLED.get());
        latency.setInterval(LATENCY_INTERVAL.get());
        config.getTelemetry().put("latency", latency);

        return config;
    }
}
