package gg.cartograph.plugin.common;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player session start times for computing session duration at leave time.
 *
 * <p>Shared between the join and leave listeners on each platform: the join
 * listener calls {@link #trackJoin(UUID)} when a player connects, and the
 * leave listener calls {@link #trackLeave(UUID)} to atomically retrieve and
 * remove the start time, returning the session duration in milliseconds.</p>
 */
public class SessionTracker
{

    private final ConcurrentHashMap<UUID, Long> joinTimes = new ConcurrentHashMap<>();

    public void trackJoin(UUID uuid)
    {
        joinTimes.put(uuid, System.currentTimeMillis());
    }

    /**
     * Records a player leaving and returns the session duration in milliseconds,
     * or {@code null} if no matching join was tracked (e.g. the player was
     * already online when the plugin started).
     */
    public Long trackLeave(UUID uuid)
    {
        var joinTime = joinTimes.remove(uuid);
        return joinTime != null ? System.currentTimeMillis() - joinTime : null;
    }
}
