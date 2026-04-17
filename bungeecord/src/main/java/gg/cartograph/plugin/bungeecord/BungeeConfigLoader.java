package gg.cartograph.plugin.bungeecord;

import gg.cartograph.plugin.common.config.CartographConfig;
import gg.cartograph.plugin.common.config.TelemetryConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class BungeeConfigLoader
{

    public static CartographConfig load(CartographBungeePlugin plugin) throws IOException
    {
        var dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists())
        {
            dataFolder.mkdirs();
        }

        var configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists())
        {
            try (var in = plugin.getResourceAsStream("config.yml"))
            {
                Files.copy(in, configFile.toPath());
            }
        }

        var fileConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        return fromConfiguration(fileConfig);
    }

    static CartographConfig fromConfiguration(Configuration section)
    {
        var config = CartographConfig.defaults();

        config.setApiKey(section.getString("api-key", config.getApiKey()));
        config.setApiEndpoint(section.getString("api-endpoint", config.getApiEndpoint()));

        var flagsSection = section.getSection("flags");
        if (flagsSection != null)
        {
            for (String key : flagsSection.getKeys())
            {
                config.getFlags().put(key, flagsSection.getBoolean(key, config.getFlags().getOrDefault(key, false)));
            }
        }

        var bufferSection = section.getSection("buffer");
        if (bufferSection != null)
        {
            var buffer = config.getBuffer();
            buffer.setSizeThreshold(bufferSection.getInt("size-threshold", buffer.getSizeThreshold()));
            buffer.setTimeThreshold(bufferSection.getInt("time-threshold", buffer.getTimeThreshold()));
            buffer.setMaxRetries(bufferSection.getInt("max-retries", buffer.getMaxRetries()));
        }

        var telemetrySection = section.getSection("telemetry");
        if (telemetrySection != null)
        {
            for (String key : telemetrySection.getKeys())
            {
                var typeSection = telemetrySection.getSection(key);
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
