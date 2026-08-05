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
    void readsFailureModeAndDiskAndBackoff()
    {
        var yaml = new YamlConfiguration();
        yaml.set("buffer.failure-mode", "memory");
        yaml.set("buffer.disk.max-size-mb", 16);
        yaml.set("buffer.disk.max-age-hours", 12);
        yaml.set("buffer.backoff.max-seconds", 120);
        yaml.set("buffer.backoff.retry-after-cap-seconds", 600);
        var config = BukkitConfigLoader.fromSection(yaml);
        assertEquals(gg.cartograph.plugin.common.config.FailureMode.MEMORY, config.getBuffer().getFailureMode());
        assertEquals(16, config.getBuffer().getDisk().getMaxSizeMb());
        assertEquals(12, config.getBuffer().getDisk().getMaxAgeHours());
        assertEquals(120, config.getBuffer().getBackoff().getMaxSeconds());
        assertEquals(600, config.getBuffer().getBackoff().getRetryAfterCapSeconds());
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
        yaml.set("telemetry.chunk-loads.interval", 45);

        var config = BukkitConfigLoader.fromSection(yaml);

        assertTrue(config.getTelemetry().containsKey("chunk-loads"));
        var chunkLoads = config.getTelemetry().get("chunk-loads");
        assertTrue(chunkLoads.isEnabled());
        assertEquals(45, chunkLoads.getInterval());
    }
}
