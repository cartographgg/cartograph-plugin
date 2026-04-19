package gg.cartograph.plugin.velocity;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.config.TelemetryConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Reads Velocity YAML configuration and maps it into a {@link CartographConfig}.
 *
 * <p>Unlike the Bukkit and BungeeCord loaders which use platform-provided YAML APIs,
 * this loader uses SnakeYAML directly since Velocity does not bundle a config
 * abstraction. The YAML is parsed into raw {@code Map<String, Object>} structures
 * and then mapped into the config model manually.</p>
 *
 * <p>This loader only reads values from the config file — it never writes, modifies,
 * or transmits the file contents. All values fall back to the defaults defined in
 * {@link CartographConfig#defaults()} if the corresponding YAML key is absent.</p>
 */
public class VelocityConfigLoader
{

    /**
     * Loads configuration from a YAML file in the given data directory.
     *
     * <p>Creates the data directory and copies the default {@code config.yml} from
     * the classpath if they do not already exist. Returns defaults if the file
     * parses as empty.</p>
     *
     * @param dataDirectory the plugin's data directory, provided by Velocity
     * @return a fully populated configuration, with defaults for any missing values
     * @throws IOException if the config file cannot be read or the default cannot be copied
     */
    @SuppressWarnings("unchecked")
    public static CartographConfig load(Path dataDirectory) throws IOException
    {
        Files.createDirectories(dataDirectory);

        var configPath = dataDirectory.resolve("config.yml");

        if (!Files.exists(configPath))
        {
            // Copy the default config bundled in the JAR to the plugin's data directory
            try (var in = VelocityConfigLoader.class.getResourceAsStream("/config.yml"))
            {
                Files.copy(in, configPath);
            }
        }

        var config = CartographConfig.defaults();

        // SnakeYAML returns null for empty files
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
