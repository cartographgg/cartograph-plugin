package gg.cartograph.plugin.common.events.telemetry;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import gg.cartograph.plugin.common.events.EventTypes;

import java.util.UUID;

/**
 * Telemetry event fired when a player connects to a node.
 *
 * <p>On Bukkit nodes this corresponds to {@code PlayerJoinEvent}. On proxy
 * nodes (Velocity, BungeeCord) this corresponds to the player completing the
 * initial connection to the proxy, not to any specific backend server.</p>
 *
 * <p>The event carries the player's UUID and current username — username
 * history is reconstructed on the ingestion side by detecting when a known
 * UUID reports a name it hasn't used before.</p>
 *
 * @param timestamp the event timestamp (epoch ms)
 * @param uuid      the player's Minecraft UUID
 * @param username  the player's current username at join time
 * @param firstJoin true if this is the player's first join on this node
 * @param protocol  the client's Minecraft protocol version, or null if unavailable
 * @param locale    the client's locale (e.g. {@code en_GB}), or null if not yet known
 * @param world     the world the player spawned into, or null on proxy nodes
 * @param bedrock   true if the player connected via Floodgate (Bedrock Edition)
 * @param ipHash    salted hash of the player's IP address — see class doc for hashing scheme
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayerJoinTelemetryEvent(
        @JsonGetter("ts") Long timestamp,
        @JsonGetter("u") UUID uuid,
        @JsonGetter("n") String username,
        @JsonGetter("fj") Boolean firstJoin,
        @JsonGetter("pr") Integer protocol,
        @JsonGetter("lo") String locale,
        @JsonGetter("w") String world,
        @JsonGetter("be") Boolean bedrock,
        @JsonGetter("ih") String ipHash
) implements TelemetryEvent
{
    @Override
    @JsonGetter("t")
    public String type()
    {
        return EventTypes.PLAYER_JOIN;
    }
}