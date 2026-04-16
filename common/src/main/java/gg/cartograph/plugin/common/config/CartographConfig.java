package gg.cartograph.plugin.common.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartographConfig
{

    private String apiKey = "";

    private String apiEndpoint = "https://api.cartograph.gg";

    private Map<String, Boolean> flags = new LinkedHashMap<>();

    private BufferConfig buffer = new BufferConfig();

    private Map<String, TelemetryConfig> telemetry = new LinkedHashMap<>();

    public static CartographConfig defaults()
    {
        var config = new CartographConfig();

        config.flags.put("report-plugins", false);

        var heartbeat = new TelemetryConfig();
        heartbeat.setInterval(60);
        config.telemetry.put("heartbeat", heartbeat);

        var tpsSample = new TelemetryConfig();
        tpsSample.setInterval(20);
        config.telemetry.put("tps_sample", tpsSample);

        var latency = new TelemetryConfig();
        latency.setInterval(30);
        config.telemetry.put("latency", latency);

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
