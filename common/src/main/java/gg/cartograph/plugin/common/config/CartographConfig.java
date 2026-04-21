package gg.cartograph.plugin.common.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root configuration model for the Cartograph plugin, shared across all platforms.
 *
 * <p>This class is a plain data object — it performs no network I/O, file access, or
 * side effects of any kind. Platform-specific config loaders populate it from their
 * respective configuration formats (YAML, TOML, ForgeConfigSpec, etc.).</p>
 *
 * <h3>What the plugin sends</h3>
 * <p>All data transmitted to the Cartograph API is governed by this configuration:</p>
 * <ul>
 *     <li><b>apiKey</b> — authenticates the server with the Cartograph API</li>
 *     <li><b>apiEndpoint</b> — the URL all telemetry is sent to (defaults to
 *         {@code https://api.cartograph.gg})</li>
 *     <li><b>flags</b> — feature toggles (e.g. {@code report-plugins} controls whether
 *         the list of installed plugins/mods is included in telemetry)</li>
 *     <li><b>telemetry</b> — controls which telemetry types are enabled and how often
 *         they are collected (heartbeat, TPS sampling, player latency)</li>
 *     <li><b>buffer</b> — controls how telemetry events are batched before being sent</li>
 * </ul>
 *
 * <p>Server owners can disable any telemetry type individually, or leave the API key
 * empty to prevent all communication.</p>
 *
 * @see BufferConfig
 * @see TelemetryConfig
 */
public class CartographConfig
{

    private String apiKey = "";

    private String apiEndpoint = "https://api.cartograph.gg";

    private String ipHashSalt = "";

    private Map<String, Boolean> flags = new LinkedHashMap<>();

    private BufferConfig buffer = new BufferConfig();

    private Map<String, TelemetryConfig> telemetry = new LinkedHashMap<>();

    /**
     * Creates a new configuration pre-populated with sensible defaults.
     *
     * <p>Default telemetry types and their collection intervals:</p>
     * <ul>
     *     <li><b>heartbeat</b> — every 60 seconds</li>
     * </ul>
     *
     * <p>All telemetry types are enabled by default. The {@code report-plugins} flag
     * defaults to {@code true} (opt-out only).</p>
     *
     * @return a new config instance with default values applied
     */
    public static CartographConfig defaults()
    {
        var config = new CartographConfig();

        config.flags.put("report-plugins", true);

        var heartbeat = new TelemetryConfig();
        heartbeat.setInterval(60);
        config.telemetry.put("heartbeat", heartbeat);

        return config;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getApiEndpoint()
    {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint)
    {
        this.apiEndpoint = apiEndpoint;
    }

    public String getIpHashSalt()
    {
        return ipHashSalt;
    }

    public void setIpHashSalt(String ipHashSalt)
    {
        this.ipHashSalt = ipHashSalt;
    }

    public Map<String, Boolean> getFlags()
    {
        return flags;
    }

    public void setFlags(Map<String, Boolean> flags)
    {
        this.flags = flags;
    }

    public BufferConfig getBuffer()
    {
        return buffer;
    }

    public void setBuffer(BufferConfig buffer)
    {
        this.buffer = buffer;
    }

    public Map<String, TelemetryConfig> getTelemetry()
    {
        return telemetry;
    }

    public void setTelemetry(Map<String, TelemetryConfig> telemetry)
    {
        this.telemetry = telemetry;
    }
}
