package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A server-provided resource pack configured at the node level.
 *
 * <p>Collected from the server's resource pack configuration at boot. The URL
 * is transmitted as-is — it's already public (every connecting player fetches
 * it) so there's no privacy concern. The hash is useful for the dashboard to
 * detect when a pack has been updated without changing URL.</p>
 *
 * @param url      the pack's download URL
 * @param hash     SHA-1 hash of the pack, or null if not configured
 * @param required true if players must accept the pack to play
 * @param prompt   the prompt message shown to players, or null if unset
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourcePackInfo(
        @JsonGetter("u") String url,
        @JsonGetter("h") String hash,
        @JsonGetter("rq") Boolean required,
        @JsonGetter("p") String prompt
)
{
}