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

    /**
     * Best-effort capture of whether the backing TOML config file already existed on
     * disk before this spec was built. NeoForge does not expose first-run state
     * directly, so this checks the conventional config path
     * ({@code config/cartograph-common.toml}) at class-init time, before the config
     * tracker gets a chance to write the default file to disk.
     */
    private static final boolean FIRST_RUN;

    private static final ModConfigSpec.ConfigValue<String> API_KEY;

    private static final ModConfigSpec.ConfigValue<String> API_ENDPOINT;

    private static final ModConfigSpec.BooleanValue FLAG_REPORT_PLUGINS;

    private static final ModConfigSpec.BooleanValue FLAG_PROXY_BACKEND;

    private static final ModConfigSpec.IntValue BUFFER_SIZE_THRESHOLD;

    private static final ModConfigSpec.IntValue BUFFER_TIME_THRESHOLD;

    private static final ModConfigSpec.IntValue BUFFER_MAX_RETRIES;

    private static final ModConfigSpec.BooleanValue HEARTBEAT_ENABLED;

    private static final ModConfigSpec.IntValue HEARTBEAT_INTERVAL;


    static {
        FIRST_RUN = !configFileExistsBestEffort();

        var builder = new ModConfigSpec.Builder();

        API_KEY = builder
                .comment("API authentication key for cartograph.gg")
                .define("api-key", "");

        API_ENDPOINT = builder
                .comment("API endpoint URL")
                .define("api-endpoint", "https://api.cartograph.gg");

        builder.comment("Feature flags").push("flags");

        FLAG_REPORT_PLUGINS = builder
                .comment(
                        "Report the list of installed plugins (names + versions, never published per-server).",
                        "On by default; set to false to opt out.")
                .define("report-plugins", true);

        FLAG_PROXY_BACKEND = builder
                .comment(
                        "Set true on a backend server behind a proxy so it does not double-count sessions",
                        "the proxy already reports.")
                .define("proxy-backend", false);

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
     * Best-effort check for whether the mod's TOML file already exists at NeoForge's
     * conventional config path ({@code <modid>-common.toml} under the config
     * directory), before the config tracker writes the default file to disk. Falls
     * back to {@code true} (i.e. not first-run) if the path cannot be determined, so
     * a failure here never spams the first-run disclosure notice.
     *
     * @return {@code true} if the config file already existed, {@code false} otherwise
     */
    private static boolean configFileExistsBestEffort()
    {
        try {
            var path = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("cartograph-common.toml");
            return java.nio.file.Files.exists(path);
        } catch (Exception e) {
            return true;
        }
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
        config.getFlags().put("proxy-backend", FLAG_PROXY_BACKEND.get());

        var buffer = new BufferConfig();
        buffer.setSizeThreshold(BUFFER_SIZE_THRESHOLD.get());
        buffer.setTimeThreshold(BUFFER_TIME_THRESHOLD.get());
        buffer.setMaxRetries(BUFFER_MAX_RETRIES.get());
        config.setBuffer(buffer);

        var heartbeat = new TelemetryConfig();
        heartbeat.setEnabled(HEARTBEAT_ENABLED.get());
        heartbeat.setInterval(HEARTBEAT_INTERVAL.get());
        config.getTelemetry().put("heartbeat", heartbeat);

        config.setFirstRun(FIRST_RUN);

        return config;
    }
}
