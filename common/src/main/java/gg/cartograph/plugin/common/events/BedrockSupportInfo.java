package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bedrock client support state, sourced from Geyser and Floodgate.
 *
 * <p>Booleans are boxed and use presence-as-truth semantics: a {@code true}
 * value means "detected"; a {@code null} value means "not detected" and is
 * omitted from the serialised payload by {@link JsonInclude.Include#NON_NULL}.
 * A {@code false} value is not produced — absence is signalled by
 * {@code null}, not by an explicit false.</p>
 *
 * @param geyser            {@code true} when the Geyser API class is loadable;
 *                          {@code null} otherwise
 * @param floodgate         {@code true} when the Floodgate API class is
 *                          loadable; {@code null} otherwise
 * @param floodgatePrefix   the configured Floodgate username prefix, or
 *                          {@code null} when Floodgate is absent
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BedrockSupportInfo(
        @JsonGetter("g") Boolean geyser,
        @JsonGetter("f") Boolean floodgate,
        @JsonGetter("fp") String floodgatePrefix
)
{
}
