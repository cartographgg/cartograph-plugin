package gg.cartograph.plugin.bukkit;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.config.TelemetryConfig;
import org.bukkit.configuration.ConfigurationSection;

public class BukkitConfigLoader
{

    public static CartographConfig load(CartographBukkitPlugin plugin)
    {
        plugin.saveDefaultConfig();
        return fromSection(plugin.getConfig());
    }

    static CartographConfig fromSection(ConfigurationSection section)
    {
        var config = CartographConfig.defaults();

        config.setApiKey(section.getString("api-key", config.getApiKey()));
        config.setApiEndpoint(section.getString("api-endpoint", config.getApiEndpoint()));

        var flagsSection = section.getConfigurationSection("flags");
        if (flagsSection != null)
        {
            for (String key : flagsSection.getKeys(false))
            {
                config.getFlags().put(key, flagsSection.getBoolean(key, config.getFlags().getOrDefault(key, false)));
            }
        }

        var bufferSection = section.getConfigurationSection("buffer");
        if (bufferSection != null)
        {
            var buffer = config.getBuffer();
            buffer.setSizeThreshold(bufferSection.getInt("size-threshold", buffer.getSizeThreshold()));
            buffer.setTimeThreshold(bufferSection.getInt("time-threshold", buffer.getTimeThreshold()));
            buffer.setMaxRetries(bufferSection.getInt("max-retries", buffer.getMaxRetries()));
        }

        var telemetrySection = section.getConfigurationSection("telemetry");
        if (telemetrySection != null)
        {
            for (String key : telemetrySection.getKeys(false))
            {
                var typeSection = telemetrySection.getConfigurationSection(key);
                if (typeSection == null)
                {
                    continue;
                }

                var telemetry = config.getTelemetry().getOrDefault(key, new TelemetryConfig());

                telemetry.setEnabled(typeSection.getBoolean("enabled", telemetry.isEnabled()));
                telemetry.setInterval(typeSection.getInt("interval", telemetry.getInterval()));

                config.getTelemetry().put(key, telemetry);
            }
        }

        return config;
    }
}
