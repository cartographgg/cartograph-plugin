package gg.cartograph.plugin.common;

/**
 * Platform-agnostic lifecycle contract for the Cartograph plugin.
 *
 * <p>Each supported server platform (Bukkit, BungeeCord, Velocity, Forge, NeoForge)
 * provides its own implementation of this interface, translating platform-specific
 * lifecycle events into these common enable/disable hooks.</p>
 *
 * <p>No network I/O or data collection occurs outside the enable/disable lifecycle.
 * Disabling the plugin halts all telemetry and API communication.</p>
 */
public interface CartographPlugin
{
    /**
     * Called when the plugin is enabled by the server.
     * Implementations should load configuration and start any telemetry collectors here.
     */
    void enable();

    /**
     * Called when the plugin is disabled by the server.
     * Implementations should stop all telemetry collectors and release resources here.
     */
    void disable();
}
