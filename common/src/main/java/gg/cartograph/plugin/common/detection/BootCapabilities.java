package gg.cartograph.plugin.common.detection;

import gg.cartograph.plugin.common.events.BedrockSupportInfo;
import gg.cartograph.plugin.common.events.ClientVersionInfo;
import gg.cartograph.plugin.common.logging.CartographLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Detects client-version and Bedrock support capabilities by reflectively
 * probing each target plugin's public API class.
 *
 * <p>All detection is by {@link Class#forName(String)} against the API class
 * — not by {@code PluginManager.getPlugin(name)} — so the same code works on
 * every platform that hosts the target plugin (Bukkit, Velocity, BungeeCord,
 * NeoForge). A target class that is not on the classpath produces a
 * {@code null} return; a class that is present but whose API call fails is
 * logged at {@code WARN} and also returns {@code null}.</p>
 *
 * <p>Logging is routed through the caller's {@link CartographLogger} because
 * the {@code common} module's SLF4J dependency is {@code compileOnly} — only
 * Velocity provides SLF4J at runtime, so a static SLF4J logger would
 * {@code NoClassDefFoundError} on Bukkit / BungeeCord / NeoForge.</p>
 */
public final class BootCapabilities
{

    private BootCapabilities()
    {
    }

    /**
     * Probes ViaVersion for the set of supported client protocol versions.
     * Returns {@code null} when ViaVersion is not on the classpath, when its
     * API surface has drifted (logged at {@code WARN}), or when its supported
     * version set is empty.
     */
    public static ClientVersionInfo detectClientVersion(CartographLogger logger)
    {
        try {
            var viaClass = Class.forName("com.viaversion.viaversion.api.Via");
            var api = viaClass.getMethod("getAPI").invoke(null);
            var versions = (java.util.SortedSet<?>) api.getClass()
                    .getMethod("getSupportedProtocolVersions")
                    .invoke(api);
            return buildClientVersion("viaversion", versions, logger);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Throwable e) {
            logger.warn("ViaVersion present but API call failed: " + e);
            return null;
        }
    }

    /**
     * Probes Geyser and Floodgate independently. Returns {@code null} when
     * neither plugin is on the classpath.
     */
    public static BedrockSupportInfo detectBedrockSupport(CartographLogger logger)
    {
        Boolean geyser = detectGeyser(logger);
        FloodgateResult floodgate = detectFloodgate(logger);
        if (geyser == null && floodgate == null) {
            return null;
        }
        return new BedrockSupportInfo(
                geyser,
                floodgate != null ? Boolean.TRUE : null,
                floodgate != null ? floodgate.prefix : null
        );
    }

    private static Boolean detectGeyser(CartographLogger logger)
    {
        try {
            Class.forName("org.geysermc.geyser.api.GeyserApi");
            return Boolean.TRUE;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Throwable e) {
            logger.warn("Geyser API class present but failed to load: " + e);
            return null;
        }
    }

    private static FloodgateResult detectFloodgate(CartographLogger logger)
    {
        try {
            var floodgateClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            var api = floodgateClass.getMethod("getInstance").invoke(null);
            var prefix = (String) api.getClass().getMethod("getPlayerPrefix").invoke(api);
            return new FloodgateResult(prefix);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Throwable e) {
            logger.warn("Floodgate present but API call failed: " + e);
            return null;
        }
    }

    private record FloodgateResult(String prefix)
    {
    }

    /**
     * Pure transformation helper. Takes the raw collection of version objects
     * returned by ViaVersion's API and produces a {@link ClientVersionInfo}.
     * Iteration order of the collection is taken as the version order
     * (ViaVersion uses {@code SortedSet}).
     *
     * @param source              wire value for {@link ClientVersionInfo#source}
     * @param versionObjects      elements with reflectively-callable {@code getVersion()}
     *                            and {@code getName()} getters
     * @param logger              destination for any reflection-failure warning
     * @return populated {@link ClientVersionInfo}, or {@code null} when the
     *         input collection is empty or reflection fails
     */
    static ClientVersionInfo buildClientVersion(
            String source,
            Collection<?> versionObjects,
            CartographLogger logger
    )
    {
        if (versionObjects.isEmpty()) {
            return null;
        }
        var protocols = new ArrayList<Integer>(versionObjects.size());
        String first = null;
        String last = null;
        try {
            for (var v : versionObjects) {
                var protocol = (int) v.getClass().getMethod("getVersion").invoke(v);
                var name = (String) v.getClass().getMethod("getName").invoke(v);
                protocols.add(protocol);
                if (first == null) {
                    first = name;
                }
                last = name;
            }
        } catch (ReflectiveOperationException e) {
            logger.warn("Failed to extract client-version info from " + source + ": " + e);
            return null;
        }
        return new ClientVersionInfo(source, first, last, List.copyOf(protocols));
    }
}
