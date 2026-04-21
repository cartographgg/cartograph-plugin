package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Single mod entry in the boot snapshot, reported on Forge, NeoForge, and
 * Fabric nodes.
 *
 * <p>Parallels {@link PluginInfo} for the Bukkit ecosystem — the two exist as
 * separate types because the concepts are distinct (mods affect both client
 * and server; plugins are server-only) and because the dashboard displays
 * them in separate sections.</p>
 *
 * <p>{@code id} is the mod loader's internal identifier ({@code modid} on
 * Forge/NeoForge, mod ID in {@code fabric.mod.json} on Fabric) — stable across
 * versions and suitable as a lookup key. {@code name} is the human-readable
 * display name from the mod's metadata, which may change between versions.</p>
 *
 * @param id      the mod loader's internal identifier (e.g. {@code jei},
 *                {@code create})
 * @param name    the human-readable display name
 * @param version the mod's version string as reported by its metadata
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ModInfo(
        @JsonGetter("i") String id,
        @JsonGetter("n") String name,
        @JsonGetter("v") String version
)
{
}