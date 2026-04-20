package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.config.TelemetryConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Reads BungeeCord YAML configuration and maps it into a {@link CartographConfig}.
 *
 * <p>This loader only reads values from the config file — it never writes, modifies,
 * or transmits the file contents. All values fall back to the defaults defined in
 * {@link CartographConfig#defaults()} if the corresponding YAML key is absent.</p>
 */
public class BungeeConfigLoader
{

    /**
     * Loads configuration from the plugin's data folder.
     *
     * <p>If the data folder or config file does not yet exist, the default
     * {@code config.yml} bundled in the plugin JAR is copied to disk first.
     * The file is then parsed via BungeeCord's YAML configuration provider.</p>
     *
     * @param plugin the BungeeCord plugin instance to load configuration from
     *
     * @return a fully populated configuration, with defaults for any missing values
     *
     * @throws IOException if the config file cannot be read or the default cannot be copied
     */
    public static CartographConfig load(CartographBungeePlugin plugin) throws IOException
    {
        var dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        var configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            // Copy the default config bundled in the JAR to the plugin's data folder
            try (var in = plugin.getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            }
        }

        var fileConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        return fromConfiguration(fileConfig);
    }

    /**
     * Parses a BungeeCord {@link Configuration} into a {@link CartographConfig}.
     *
     * <p>Each section is read independently with fallback defaults, so partial configs
     * are safe. Custom telemetry types defined by the user are also supported — any
     * subsection under {@code telemetry} will be loaded, not just the built-in types.</p>
     *
     * @param section the root configuration to read from
     *
     * @return a fully populated configuration
     */
    static CartographConfig fromConfiguration(Configuration section)
    {
        var config = CartographConfig.defaults();

        config.setApiKey(section.getString("api-key", config.getApiKey()));
        config.setApiEndpoint(section.getString("api-endpoint", config.getApiEndpoint()));

        var flagsSection = section.getSection("flags");
        if (flagsSection != null) {
            for (String key : flagsSection.getKeys()) {
                config.getFlags().put(key, flagsSection.getBoolean(key, config.getFlags().getOrDefault(key, false)));
            }
        }

        var bufferSection = section.getSection("buffer");
        if (bufferSection != null) {
            var buffer = config.getBuffer();
            buffer.setSizeThreshold(bufferSection.getInt("size-threshold", buffer.getSizeThreshold()));
            buffer.setTimeThreshold(bufferSection.getInt("time-threshold", buffer.getTimeThreshold()));
            buffer.setMaxRetries(bufferSection.getInt("max-retries", buffer.getMaxRetries()));
        }

        var telemetrySection = section.getSection("telemetry");
        if (telemetrySection != null) {
            for (String key : telemetrySection.getKeys()) {
                var typeSection = telemetrySection.getSection(key);
                if (typeSection == null) {
                    continue;
                }

                // Reuse existing defaults if this is a known telemetry type,
                // otherwise create a new config for custom types
                var telemetry = config.getTelemetry().getOrDefault(key, new TelemetryConfig());

                telemetry.setEnabled(typeSection.getBoolean("enabled", telemetry.isEnabled()));
                telemetry.setInterval(typeSection.getInt("interval", telemetry.getInterval()));

                config.getTelemetry().put(key, telemetry);
            }
        }

        return config;
    }
}
