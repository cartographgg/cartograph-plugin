package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Range of Minecraft client protocol versions a server accepts, as reported
 * by ViaVersion.
 *
 * @param source     name of the reporting plugin (currently always
 *                   {@code "viaversion"})
 * @param min        lowest supported Minecraft version string (e.g.
 *                   {@code "1.8"})
 * @param max        highest supported Minecraft version string
 * @param protocols  every supported protocol version number
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClientVersionInfo(
        @JsonGetter("s") String source,
        @JsonGetter("min") String min,
        @JsonGetter("max") String max,
        @JsonGetter("p") List<Integer> protocols
)
{
}
