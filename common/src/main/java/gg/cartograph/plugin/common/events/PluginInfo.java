package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Single plugin entry in the boot snapshot.
 */
public record PluginInfo(
        @JsonGetter("n") String name,
        @JsonGetter("v") String version,
        @JsonGetter("e") boolean enabled
)
{
}