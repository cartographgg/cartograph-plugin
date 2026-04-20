package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Operating system snapshot captured at plugin boot.
 */
public record OsInfo(
        @JsonGetter("n") String name,
        @JsonGetter("v") String version,
        @JsonGetter("a") String arch
)
{
}