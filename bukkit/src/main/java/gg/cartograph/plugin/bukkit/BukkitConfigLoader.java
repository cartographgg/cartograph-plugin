package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.config.TelemetryConfig;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Reads Bukkit YAML configuration and maps it into a {@link CartographConfig}.
 *
 * <p>This loader only reads values from the config file — it never writes, modifies,
 * or transmits the file contents. All values fall back to the defaults defined in
 * {@link CartographConfig#defaults()} if the corresponding YAML key is absent.</p>
 */
public class BukkitConfigLoader
{

    /**
     * Loads configuration from the plugin's {@code config.yml}.
     *
     * <p>Ensures the default config file exists on disk (via {@code saveDefaultConfig}),
     * then parses the YAML into a {@link CartographConfig}.</p>
     *
     * @param plugin the Bukkit plugin instance to load configuration from
     *
     * @return a fully populated configuration, with defaults for any missing values
     */
    public static CartographConfig load(CartographBukkitPlugin plugin)
    {
        plugin.saveDefaultConfig();
        return fromSection(plugin.getConfig());
    }

    /**
     * Parses a Bukkit {@link ConfigurationSection} into a {@link CartographConfig}.
     *
     * <p>Each section is read independently with fallback defaults, so partial configs
     * are safe. Custom telemetry types defined by the user are also supported — any
     * subsection under {@code telemetry} will be loaded, not just the built-in types.</p>
     *
     * @param section the root configuration section to read from
     *
     * @return a fully populated configuration
     */
    static CartographConfig fromSection(ConfigurationSection section)
    {
        var config = CartographConfig.defaults();

        config.setApiKey(section.getString("api-key", config.getApiKey()));
        config.setApiEndpoint(section.getString("api-endpoint", config.getApiEndpoint()));
        config.setIpHashSalt(section.getString("ip-hash-salt", config.getIpHashSalt()));

        var flagsSection = section.getConfigurationSection("flags");
        if (flagsSection != null) {
            for (String key : flagsSection.getKeys(false)) {
                config.getFlags().put(key, flagsSection.getBoolean(key, config.getFlags().getOrDefault(key, false)));
            }
        }

        var bufferSection = section.getConfigurationSection("buffer");
        if (bufferSection != null) {
            var buffer = config.getBuffer();
            buffer.setSizeThreshold(bufferSection.getInt("size-threshold", buffer.getSizeThreshold()));
            buffer.setTimeThreshold(bufferSection.getInt("time-threshold", buffer.getTimeThreshold()));
            buffer.setMaxRetries(bufferSection.getInt("max-retries", buffer.getMaxRetries()));
        }

        var telemetrySection = section.getConfigurationSection("telemetry");
        if (telemetrySection != null) {
            for (String key : telemetrySection.getKeys(false)) {
                var typeSection = telemetrySection.getConfigurationSection(key);
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
