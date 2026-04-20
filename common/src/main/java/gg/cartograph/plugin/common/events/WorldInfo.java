package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Single world entry in the boot snapshot.
 *
 * <p>{@code environment} is a lowercase string ({@code normal}, {@code nether},
 * {@code the_end}, {@code custom}) rather than an enum to preserve flexibility
 * for custom dimensions.</p>
 */
public record WorldInfo(
        @JsonGetter("n") String name,
        @JsonGetter("e") String environment
)
{
}