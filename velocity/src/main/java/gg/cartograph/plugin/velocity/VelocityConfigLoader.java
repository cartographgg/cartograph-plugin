package gg.cartograph.plugin.velocity;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.config.TelemetryConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class VelocityConfigLoader
{

    @SuppressWarnings("unchecked")
    public static CartographConfig load(Path dataDirectory) throws IOException
    {
        Files.createDirectories(dataDirectory);

        var configPath = dataDirectory.resolve("config.yml");

        if (!Files.exists(configPath))
        {
            try (var in = VelocityConfigLoader.class.getResourceAsStream("/config.yml"))
            {
                Files.copy(in, configPath);
            }
        }

        var config = CartographConfig.defaults();

        Map<String, Object> data;
        try (var reader = Files.newBufferedReader(configPath))
        {
            data = new Yaml().load(reader);
        }

        if (data == null)
        {
            return config;
        }

        if (data.containsKey("api-key"))
        {
            config.setApiKey((String) data.get("api-key"));
        }

        if (data.containsKey("api-endpoint"))
        {
            config.setApiEndpoint((String) data.get("api-endpoint"));
        }

        var flagsMap = (Map<String, Object>) data.get("flags");
        if (flagsMap != null)
        {
            for (var entry : flagsMap.entrySet())
            {
                config.getFlags().put(entry.getKey(), (Boolean) entry.getValue());
            }
        }

        var bufferMap = (Map<String, Object>) data.get("buffer");
        if (bufferMap != null)
        {
            var buffer = config.getBuffer();
            if (bufferMap.containsKey("size-threshold"))
            {
                buffer.setSizeThreshold((Integer) bufferMap.get("size-threshold"));
            }
            if (bufferMap.containsKey("time-threshold"))
            {
                buffer.setTimeThreshold((Integer) bufferMap.get("time-threshold"));
            }
            if (bufferMap.containsKey("max-retries"))
            {
                buffer.setMaxRetries((Integer) bufferMap.get("max-retries"));
            }
        }

        var telemetryMap = (Map<String, Object>) data.get("telemetry");
        if (telemetryMap != null)
        {
            for (var entry : telemetryMap.entrySet())
            {
                var typeData = (Map<String, Object>) entry.getValue();
                if (typeData == null)
                {
                    continue;
                }

                var telemetry = config.getTelemetry().getOrDefault(entry.getKey(), new TelemetryConfig());

                if (typeData.containsKey("enabled"))
                {
                    telemetry.setEnabled((Boolean) typeData.get("enabled"));
                }
                if (typeData.containsKey("interval"))
                {
                    telemetry.setInterval((Integer) typeData.get("interval"));
                }

                config.getTelemetry().put(entry.getKey(), telemetry);
            }
        }

        return config;
    }
}
