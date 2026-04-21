package gg.cartograph.plugin.common.events.telemetry;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import gg.cartograph.plugin.common.events.EventTypes;
import gg.cartograph.plugin.common.events.LeaveReason;

import java.util.UUID;

/**
 * Telemetry event fired when a player disconnects from a node.
 *
 * <p>On Bukkit nodes this corresponds to {@code PlayerQuitEvent} and its
 * kick/ban siblings. On proxy nodes (Velocity, BungeeCord) this corresponds
 * to the player's connection to the proxy closing, regardless of which
 * backend they were on.</p>
 *
 * <p>The ingestion side pairs this event with a prior {@code PLAYER_JOIN}
 * from the same node and UUID within a reasonable time window to materialise
 * a session record. Sessions are derived server-side, not transmitted by the
 * plugin — this keeps crashes and missing leave events from creating
 * duplicate or corrupt sessions.</p>
 *
 * @param timestamp       the event timestamp (epoch ms)
 * @param uuid            the player's Minecraft UUID
 * @param sessionDuration time since the matching join, in milliseconds
 * @param reason          the classified reason for the disconnect — see {@link LeaveReason}
 * @param world           the world the player was in at time of disconnect, or null on proxy nodes
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayerLeaveTelemetryEvent(
        @JsonGetter("ts") Long timestamp,
        @JsonGetter("u") UUID uuid,
        @JsonGetter("sd") Long sessionDuration,
        @JsonGetter("r") LeaveReason reason,
        @JsonGetter("w") String world
) implements TelemetryEvent
{
    @Override
    @JsonGetter("t")
    public String type()
    {
        return EventTypes.PLAYER_LEAVE;
    }
}