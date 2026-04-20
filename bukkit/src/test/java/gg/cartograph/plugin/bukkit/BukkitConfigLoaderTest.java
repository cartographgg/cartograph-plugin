package gg.cartograph.plugin.bukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BukkitConfigLoaderTest
{

    @Test
    void emptyConfigReturnsDefaults()
    {
        var yaml = new YamlConfiguration();

        var config = BukkitConfigLoader.fromSection(yaml);

        assertEquals("", config.getApiKey());
        assertEquals("https://api.cartograph.gg", config.getApiEndpoint());
        assertTrue(config.getFlags().get("report-plugins"));
        assertEquals(50, config.getBuffer().getSizeThreshold());
        assertTrue(config.getTelemetry().containsKey("heartbeat"));
    }

    @Test
    void overridesTopLevelValues()
    {
        var yaml = new YamlConfiguration();
        yaml.set("api-key", "test-key-123");
        yaml.set("api-endpoint", "https://staging.cartograph.gg");

        var config = BukkitConfigLoader.fromSection(yaml);

        assertEquals("test-key-123", config.getApiKey());
        assertEquals("https://staging.cartograph.gg", config.getApiEndpoint());
    }

    @Test
    void overridesFlagValues()
    {
        var yaml = new YamlConfiguration();
        yaml.set("flags.report-plugins", true);

        var config = BukkitConfigLoader.fromSection(yaml);

        assertTrue(config.getFlags().get("report-plugins"));
    }

    @Test
    void addsCustomFlag()
    {
        var yaml = new YamlConfiguration();
        yaml.set("flags.custom-flag", true);

        var config = BukkitConfigLoader.fromSection(yaml);

        assertTrue(config.getFlags().containsKey("custom-flag"));
        assertTrue(config.getFlags().get("custom-flag"));
    }

    @Test
    void overridesBufferValues()
    {
        var yaml = new YamlConfiguration();
        yaml.set("buffer.size-threshold", 100);
        yaml.set("buffer.time-threshold", 30);
        yaml.set("buffer.max-retries", 5);

        var config = BukkitConfigLoader.fromSection(yaml);

        assertEquals(100, config.getBuffer().getSizeThreshold());
        assertEquals(30, config.getBuffer().getTimeThreshold());
        assertEquals(5, config.getBuffer().getMaxRetries());
    }

    @Test
    void overridesTelemetryValues()
    {
        var yaml = new YamlConfiguration();
        yaml.set("telemetry.heartbeat.enabled", false);
        yaml.set("telemetry.heartbeat.interval", 120);

        var config = BukkitConfigLoader.fromSection(yaml);

        var heartbeat = config.getTelemetry().get("heartbeat");
        assertFalse(heartbeat.isEnabled());
        assertEquals(120, heartbeat.getInterval());
    }

    @Test
    void addsCustomTelemetryType()
    {
        var yaml = new YamlConfiguration();
        yaml.set("telemetry.chunk-loads.enabled", true);
        yaml.set("telemetry.chunk-loads.interval", 15);

        var config = BukkitConfigLoader.fromSection(yaml);

        assertTrue(config.getTelemetry().containsKey("chunk-loads"));
        var chunkLoads = config.getTelemetry().get("chunk-loads");
        assertTrue(chunkLoads.isEnabled());
        assertEquals(15, chunkLoads.getInterval());
    }
}
